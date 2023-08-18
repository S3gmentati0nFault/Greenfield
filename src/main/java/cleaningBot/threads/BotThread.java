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
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
                random.nextInt(100),
                random.nextInt(65534),
                "localhost");
        timestamp = -1;

        otherBots = new ThreadSafeArrayList<>();
        openComms = new ThreadSafeHashMap<>();
        botServices = new BotServices(this);
    }

    /**
     * Override of run method that starts the thread used for inter-bot communication and
     * initiates the communication channel with the administration server.
     */
    @Override
    public void run(){
        Logger.yellow("Starting grpc services");
        GrpcServicesThread grpcThread = new GrpcServicesThread(identity.getPort(), botServices);
        grpcThread.start();

        // TODO
        // CAPIRE PERCHÉ IN FASE DI SPIN-UP DEL PROGRAMMA, CI SONO ALCUNI NODI CHE COMINCIANO SUBITO IL PROCESSO DI
        // MANUTENZIONE
        if(!startNewBot()){
            Logger.red("There was an error during Thread instantiation");
        }

        synchronized (this) {
            try {
                wait(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Logger.yellow("Starting input thread");
        inputThread = new InputThread();
        inputThread.start();

        Logger.yellow("Starting maintenance thread");
        maintenanceThread = new MaintenanceThread(botServices);
        maintenanceThread.start();
//
//        Logger.yellow("Starting the pollution measurement sensor thread");
//        PollutionSensorThread pollutionSensorThread = new PollutionSensorThread(district, identity);
//        pollutionSensorThread.start();
    }

    /**
     * Method that opens a connection with the administration server and makes its
     * presence known to both the server and the other bots in the network.
     * @return It returns true if the operation went well, false otherwise.
     */
    private synchronized boolean startNewBot() {
        ObjectMapper mapper = new ObjectMapper();

        HttpURLConnection connection =
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
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            return false;
        }

        try{
            String[] responseLine = br.readLine().split("-");
            identity.setPosition(new ObjectMapper().readValue(responseLine[0], Position.class));
            List<BotIdentity> robots = new ObjectMapper().readValue(responseLine[1], new TypeReference<List<BotIdentity>>(){});
            otherBots.getArrayList().addAll(robots);
        } catch (IOException e) {
            Logger.red("It was not possible to retrieve the response from the server");
            return false;
        }

        BotUtilities.closeConnection(connection);
        otherBots.removeElement(identity);

//      TODO
//      CAPIRE PERCHÉ MI RESTITUISCE ERRORE DI CONCURRENT MODIFICATION QUANDO AGGIUNGO UN TIMER QUI SOTTO E RIVEDERE
//      COME FUNZIONA IL TUTTO
        Logger.cyan("Letting my presence known");
        if(!otherBots.isEmpty()) {
            counter = new AtomicCounter(otherBots.size());
            otherBots.getArrayList().forEach(botIdentity -> {

//                try {
//                    Logger.blue("ADD");
//                    sleep(2000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }

                ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();
                BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

                openComms.addPair(botIdentity, new CommPair(channel, serviceStub));

                BotGRPC.BotNetworkingInformations identikit = BotGRPC.BotNetworkingInformations
                        .newBuilder()
                        .setId(identity.getId())
                        .setPort(identity.getPort())
                        .setHost(identity.getIp())
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
                            Logger.yellow("Removing dead robot from the field");
                            BotUtilities.botRemovalFunction(receiver, false);
                        }
                        checkCounter();
                    }

                    @Override
                    public void onCompleted() {
                        checkCounter();
                    }
                });
            });
        }

        district = BotUtilities.districtCalculator(identity.getPosition());

        BotUtilities.closeConnection(connection);

        return true;
    }

    public synchronized void checkCounter() {
        if(counter.getCounter() == 0) {
            Logger.green("Hello procedure completed");
            notify();
        }
    }

    /**
     * Getter for the bots in the system.
     */
    public List<BotIdentity> getOtherBots() {
        return otherBots.getArrayList();
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
        for (BotIdentity botIdentity : otherBots.getArrayList()) {
            System.out.println(botIdentity);
        }
    }

    public void printOpenComms() {
        Set<BotIdentity> set = openComms.getKeySet();

        for (BotIdentity botIdentity : set) {
            System.out.println(botIdentity + " -> " + openComms.getValue(botIdentity));
        }
    }

    public synchronized boolean removeBot(BotIdentity deadRobot) {
        System.out.println(deadRobot);
        boolean removalOperation = otherBots.removeElement(deadRobot);
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
}