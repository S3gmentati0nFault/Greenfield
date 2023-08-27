package cleaningBot.threads;

import cleaningBot.CommPair;
import extra.AtomicCounter.AtomicCounter;
import extra.ThreadSafeStructures.ThreadSafeArrayList;
import extra.ThreadSafeStructures.ThreadSafeHashMap;
import utilities.Variables;
import cleaningBot.BotUtilities;
import cleaningBot.service.BotServices;
import extra.Logger.Logger;
import cleaningBot.Position;
import beans.BotIdentity;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static utilities.Variables.*;

/**
 * @see cleaningBot.CleaningBot
 * A thread for the CleaningBot class that handles the initial connection with the
 * administration server
 */
public class BotThread extends Thread{
    private ThreadSafeArrayList<BotIdentity> otherBots;
    private ThreadSafeHashMap<BotIdentity, CommPair> openComms;
    private BotIdentity identity;
    private BotServices botServices;
    private long timestamp;
    private int district;
    private MaintenanceThread maintenanceThread;
    private InputThread inputThread;
    private static BotThread instance;
    private AtomicCounter counter;
    private PollutionSensorThread pollutionSensorThread;
    private MeasurementGatheringThread measurementGatheringThread;

    public static synchronized BotThread getInstance() {
        if(instance == null) {
            instance = new BotThread();
        }
        return instance;
    }

    /**
     * Empty constructor that generates random values for both the id and the
     * port.
     */
    public BotThread(){
        Random random = new Random();

        identity = new BotIdentity(
                random.nextInt(UPPER_ID_LIMIT),
//                1,
                random.nextInt(65534),
                "localhost");
        timestamp = -1;

        otherBots = new ThreadSafeArrayList<>();
        openComms = new ThreadSafeHashMap<>();
        botServices = new BotServices(this);

        measurementGatheringThread = null;
    }

    /**
     * Override of run method that starts the thread used for inter-bot communication and
     * initiates the communication channel with the administration server.
     */
    @Override
    public synchronized void run(){
        Logger.yellow("Starting grpc services");
        GrpcServicesThread grpcThread = new GrpcServicesThread(identity.getPort(), botServices);
        grpcThread.start();

        if(!startNewBot()){
            Logger.red("There was an error during Thread instantiation");
            System.exit(-1);
        }

        Logger.yellow("Starting input thread");
        inputThread = new InputThread();
        inputThread.start();

        Logger.yellow("Starting maintenance thread");
        maintenanceThread = new MaintenanceThread(botServices);
        maintenanceThread.start();

        Logger.yellow("Starting the pollution measurement sensor thread");
        pollutionSensorThread = new PollutionSensorThread(district, identity);
        pollutionSensorThread.start();

        Logger.yellow("Starting the measurement gathering thread");
        measurementGatheringThread = new MeasurementGatheringThread();
        measurementGatheringThread.start();
    }

    /**
     * Method that opens a connection with the administration server and makes its
     * presence known to both the server and the other bots in the network.
     * @return It returns true if the operation went well, false otherwise.
     */
    private synchronized boolean startNewBot() {
        ObjectMapper mapper = new ObjectMapper();

        HttpURLConnection connection = null;
        boolean isBotAlreadyInTheNetwork = true;
        int i = 0;

        while(isBotAlreadyInTheNetwork && i < UPPER_ID_LIMIT) {
            connection =
                BotUtilities.buildConnection("GET", "http://" +
                        Variables.HOST+":" + Variables.PORT + "/admin/poll/" + identity.getId());

            BufferedReader br = null;
            try{
                br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"));
            } catch(IOException e) {
                Logger.red(":(");
                return false;
            }

            try{
                isBotAlreadyInTheNetwork = Boolean.parseBoolean(br.readLine());
                if(isBotAlreadyInTheNetwork) {
                    Random random = new Random();
                    identity.setId(random.nextInt(UPPER_ID_LIMIT));
                }
            } catch (IOException e) {
                Logger.red("It was not possible to retrieve the response from the server");
                return false;
            }
        }

        if(i > UPPER_ID_LIMIT) {
            System.exit(-1);
        }

        BotUtilities.closeConnection(connection);

        connection =
                BotUtilities.buildConnection("POST", "http://" +
                        Variables.HOST+":" + Variables.PORT + "/admin/join");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        OutputStream os;
        try{
            os = connection.getOutputStream();
        }catch(IOException e){
            Logger.red("There was an error during output stream creation");
            return false;
        }

        String json = "";
        try{
            json = mapper.writeValueAsString(identity);
        } catch (IOException e) {
            Logger.red("There was an error while generating the json string");
            return false;
        }

        byte[] input = null;
        try{
            input = json.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.red("There was a problem while turning the string into bytes");
            return false;
        }

        try{
            os.write(input, 0, input.length);
        }catch(IOException e){
            Logger.red("There was an error while writing on output stream");
            return false;
        }

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
        } catch(IOException e) {
            Logger.red("It was not possible to initialize the BufferedReader");
            return false;
        }

        try{
            String[] responseLine = br.readLine().split("-");
            identity.setPosition(new ObjectMapper().readValue(responseLine[0], Position.class));
            List<BotIdentity> robots = new ObjectMapper().readValue(responseLine[1], new TypeReference<List<BotIdentity>>(){});
            otherBots.addAll(robots);
        } catch (IOException e) {
            Logger.red("It was not possible to retrieve the response from the server");
            return false;
        }

        BotUtilities.closeConnection(connection);
        otherBots.removeElement(identity);

//      TODO
//      >> FLAVOUR :: DEBUGGING-ARANCIONE <<
//      CAPIRE PERCHÉ MI RESTITUISCE ERRORE DI CONCURRENT MODIFICATION QUANDO AGGIUNGO UN TIMER QUI SOTTO E RIVEDERE
//      COME FUNZIONA IL TUTTO
        Logger.cyan("Letting my presence known");
        if(!otherBots.isEmpty()) {
            counter = new AtomicCounter(otherBots.size());
            List<BotIdentity> nonRespondingRobots = new ArrayList<>();

            otherBots.getCopy().forEach(botIdentity -> {

                try {
                    Logger.blue("ADD");
                    sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                ManagedChannel channel = ManagedChannelBuilder
                        .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                        .usePlaintext()
                        .build();
                BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

                openComms.addPair(botIdentity, new CommPair(channel, serviceStub));

                BotGRPC.BotInformation identikit = BotGRPC.BotInformation
                        .newBuilder()
                        .setId(identity.getId())
                        .setPort(identity.getPort())
                        .setHost(identity.getIp())
                        .setPosition(BotGRPC.Position.newBuilder()
                                .setX(identity.getPosition().getX())
                                .setY(identity.getPosition().getY())
                                .build()
                        )
                        .build();

                serviceStub.joinRequestGRPC(identikit, new StreamObserver<BotGRPC.Acknowledgement>() {
                    BotIdentity receiver = botIdentity;

                    @Override
                    public void onNext(BotGRPC.Acknowledgement value) {
                        counter.decrement();
                        if(!value.getAck()){
                            Logger.purple("robot " + botIdentity + " didn't add me to its network");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Logger.red("robot " + botIdentity + " sent error " + t.getClass());
                        counter.decrement();
                        if(t.getClass() == StatusRuntimeException.class) {
                            nonRespondingRobots.add(botIdentity);
                        }
                        checkCounter(nonRespondingRobots);
                    }

                    @Override
                    public void onCompleted() {
                        checkCounter(nonRespondingRobots);
                    }
                });
            });
        }

        district = BotUtilities.districtCalculator(identity.getPosition());

        BotUtilities.closeConnection(connection);

        return true;
    }

    public synchronized void checkCounter(List<BotIdentity> nonRespondingRobots) {
        if(counter.getCounter() == 0) {
            Logger.green("Hello procedure completed");
            if(!nonRespondingRobots.isEmpty()) {
                BotUtilities.botRemovalFunction(nonRespondingRobots, false);
            }
        }
    }

    /**
     * Getter for the bots in the system.
     */
    public ThreadSafeArrayList<BotIdentity> getOtherBots() {
        return otherBots;
    }

    /**
     * Getter for the timestamp of the latest request, the timestamp is -1 if there was no
     * previous request
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the identity of the bot
     */
    public BotIdentity getIdentity() {
        return identity;
    }

    public MaintenanceThread getMaintenanceThread() {
        return maintenanceThread;
    }

    public InputThread getInputThread() {
        return inputThread;
    }

    public ThreadSafeHashMap<BotIdentity, CommPair> getOpenComms() {
        return openComms;
    }

    public BotServices getBotServices() {
        return botServices;
    }

    public int getDistrict() {
        return district;
    }

    public synchronized MeasurementGatheringThread getMeasurementGatheringThread() {
        if(measurementGatheringThread == null) {
            System.out.println("WAITING...");
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e.getCause());
            }
        }
        System.out.println("NOT WAITING ANYMORE....");
        return measurementGatheringThread;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    /**
     * Setter for the timestamp of the latest request
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Method that prints the bots present in the system
     */
    public void printOtherBots() {
        for (BotIdentity botIdentity : otherBots.getCopy()) {
            System.out.println(botIdentity);
        }
    }

    public void updatePosition(BotIdentity movedBot) {
        otherBots.swap(new BotIdentity(movedBot.getId()), movedBot);
    }

    public void printOpenComms() {
        Set<BotIdentity> set = openComms.getKeySet();

        for (BotIdentity botIdentity : set) {
            System.out.println(botIdentity + " -> " + openComms.getValue(botIdentity));
        }
    }

    public synchronized boolean removeBot(List<BotIdentity> deadRobots) {
        boolean removalOperation = true;
        for (BotIdentity deadRobot : deadRobots) {
            System.out.println(deadRobot);
            removalOperation &= otherBots.removeElement(deadRobot);
            CommPair communicationPair = openComms.removePair(deadRobot);
            if(communicationPair != null) {
                try{
                    Logger.yellow("Closing the communication channel...");
                    communicationPair.getManagedChannel().shutdown().awaitTermination(10, TimeUnit.SECONDS);
                    Logger.green("Communication channel closed!");
                } catch (InterruptedException e) {
                    Logger.red("Unable to close the channel the right way, forcing it closed now");
                    communicationPair.getManagedChannel().shutdownNow();
                }
            }
        }
        return removalOperation;
    }

    public synchronized boolean removeBot(BotIdentity deadRobot) {
        boolean removalOperation = true;
        System.out.println(deadRobot);
        removalOperation &= otherBots.removeElement(deadRobot);
        CommPair communicationPair = openComms.removePair(deadRobot);
        if(communicationPair != null) {
            try{
                Logger.yellow("Closing the communication channel...");
                communicationPair.getManagedChannel().shutdown().awaitTermination(10, TimeUnit.SECONDS);
                Logger.green("Communication channel closed!");
            } catch (InterruptedException e) {
                Logger.red("Unable to close the channel the right way, forcing it closed now");
                communicationPair.getManagedChannel().shutdownNow();
            }
        }
        return removalOperation;
    }

    public void newCommunicationChannel(BotIdentity destination, ManagedChannel channel, BotServicesGrpc.BotServicesStub stub) {
        CommPair commPair = new CommPair(channel, stub);
        openComms.addPair(destination, commPair);
    }

    public void changeMyPosition(int district) {
        Position newPosition = BotUtilities.positionCalculator(district);
        BotIdentity tmp = identity;
        identity.setPosition(newPosition);
        setDistrict(district);
        System.out.println("I'VE BEEN MOVED TO " + district + " MY NEW POSITION IS " + newPosition);

        List<BotIdentity> fleetSnapshot = otherBots.getCopy();

        for (BotIdentity botIdentity : fleetSnapshot) {
            System.out.println(botIdentity);
        }

        //        TODO
//        >> FLAVOUR :: CONSEGNA-ARANCIONE <<
//        CAMBIARE L'ISCRIZIONE MQTT DEL ROBOT
        ObjectMapper mapper = new ObjectMapper();

            HttpURLConnection connection =
                    BotUtilities.buildConnection("PUT", "http://" +
                            Variables.HOST+":" + Variables.PORT + "/admin/update");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            OutputStream os = null;
            try{
                os = connection.getOutputStream();
            }catch(IOException e){
                Logger.red("There was an error during output stream creation");
            }

            String json = "";
            try{
                json = "[" + mapper.writeValueAsString(tmp) + ", " + mapper.writeValueAsString(identity) + "]";
            } catch (IOException e) {
                Logger.red("There was an error while generating the json string");
            }

            byte[] input = null;
            try{
                input = json.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                Logger.red("There was a problem while turning the string into bytes");
            }

            try{
                os.write(input, 0, input.length);
            }catch(IOException e){
                Logger.red("There was an error while writing on output stream");
            }

            BufferedReader br = null;
            try{
                br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"));
            } catch(IOException e) {
                Logger.red("It was not possible to initialize the BufferedReader");
            }

            BotUtilities.closeConnection(connection);

            if(DEBUGGING) {
                System.out.println("->->RIMUOVERE QUESTO BOT<-<-");
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("AGGIORNANDO GLI ALTRI ROBOT");
            }

        for (BotIdentity botIdentity : fleetSnapshot) {

            ManagedChannel channel;
            BotServicesGrpc.BotServicesStub serviceStub;

            CommPair communicationPair = openComms.getValue(botIdentity);
            if(communicationPair == null) {
                channel = ManagedChannelBuilder
                        .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                        .usePlaintext()
                        .build();

                serviceStub = BotServicesGrpc.newStub(channel);
                newCommunicationChannel(botIdentity, channel, serviceStub);
            }
            else {
                channel = communicationPair.getManagedChannel();
                serviceStub = communicationPair.getCommunicationStub();
            }
            BotGRPC.BotInformation botInfo = BotGRPC.BotInformation.newBuilder()
                    .setId(identity.getId())
                    .setHost(identity.getIp())
                    .setPort(identity.getPort())
                    .setPosition(BotGRPC.Position.newBuilder()
                            .setX(newPosition.getX())
                            .setY(newPosition.getY())
                            .build())
                    .build();

//            TODO
//            >> FLAVOUR :: DEBUGGING-GIALLO <<
//            INDAGARE COMPORTAMENTI STRANI IN FASE DI MODIFICA DELLA POSIZIONE DOVUTI A NON SI SA BENE CHE COSA,
//            L'ERRORE È QUESTO QUI
//            io.grpc.Context was cancelled without error
            serviceStub.positionModificationRequestGRPC(botInfo, new StreamObserver<BotGRPC.Acknowledgement>() {
                @Override
                public void onNext(BotGRPC.Acknowledgement value) {}

                @Override
                public void onError(Throwable t) {
                    if(t.getClass() == StatusRuntimeException.class) {
                        Logger.red("The bot that was communicating with me has crashed...");
                    }
                    else {
                        Logger.red("Something has gone wrong during the update process");
                    }
                }

                @Override
                public void onCompleted() {}
            });
        }

        pollutionSensorThread.closeConnection();
        pollutionSensorThread = new PollutionSensorThread(district, identity);
    }
}