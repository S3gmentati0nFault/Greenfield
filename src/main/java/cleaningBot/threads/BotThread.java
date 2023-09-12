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

import javax.naming.CommunicationException;
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
public class BotThread extends Thread {
    private final ThreadSafeArrayList<BotIdentity> otherBots;
    private final ThreadSafeHashMap<BotIdentity, CommPair> openComms;
    private BotIdentity identity;
    private final BotServices botServices;
    private long timestamp;
    private int district;
    private MaintenanceThread maintenanceThread;
    private InputThread inputThread;
    private static BotThread instance;
    private PollutionSensorThread pollutionSensorThread;
    private MeasurementGatheringThread measurementGatheringThread;

    public static synchronized BotThread getInstance() {
        if (instance == null) {
            instance = new BotThread();
        }
        return instance;
    }

    /**
     * Empty constructor that generates random values for both the id and the
     * port.
     */
    public BotThread() {
        Random random = new Random();

        identity = new BotIdentity(
                random.nextInt(UPPER_ID_LIMIT),
                random.nextInt(65534),
                "localhost");
        timestamp = -1;

//        Logger.whiteDebuggingPrint(identity.toString(), BOT_THREAD_DEBUGGING);

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
    public synchronized void run() {
        boolean isServerRunning = false;
        Random random = new Random();
        while (!isServerRunning) {
            Logger.yellow("Starting grpc services");
            GrpcServicesThread grpcThread = new GrpcServicesThread(identity.getPort(), botServices);
            grpcThread.start();
            try {
                Logger.whiteDebuggingPrint("WAITING", DEBUGGING);
                wait();
                Logger.whiteDebuggingPrint("NOT WAITING", DEBUGGING);
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }
            isServerRunning = grpcThread.isRunning();
            if (!isServerRunning) {
                identity.setPort(random.nextInt(65534));
            }
        }

        int res = startNewBot();

        if (res == 0) {
            Logger.red("There was an error during Thread instantiation");
            System.exit(-1);
        } else if (res == 1) {
            try {
                Logger.whiteDebuggingPrint(this.getClass() + ".run IS WAITING", DEBUGGING);
                wait();
                Logger.whiteDebuggingPrint(this.getClass() + ".run IS NOT WAITING", DEBUGGING);
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

        Logger.yellow("Starting the measurement gathering thread");
        measurementGatheringThread = new MeasurementGatheringThread();
        measurementGatheringThread.start();

        Logger.yellow("Starting the pollution measurement sensor thread");
        pollutionSensorThread = new PollutionSensorThread(district, identity);
        pollutionSensorThread.start();
    }

    /**
     * Method that opens a connection with the administration server and makes its
     * presence known to both the server and the other bots in the network.
     *
     * @return It returns true if the operation went well, false otherwise.
     */
    private synchronized int startNewBot() {
        ObjectMapper mapper = new ObjectMapper();

        HttpURLConnection connection = null;
        boolean isBotAlreadyInTheNetwork = true;
        int i = 0;

        while (isBotAlreadyInTheNetwork && i < UPPER_ID_LIMIT) {
            connection =
                    BotUtilities.buildConnection("GET", "http://" +
                            Variables.HOST + ":" + Variables.PORT + "/admin/poll/" + identity.getId());

            BufferedReader br = null;
            try {
                br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"));
            } catch (IOException e) {
                Logger.red(":(");
                return 0;
            }

            try {
                isBotAlreadyInTheNetwork = Boolean.parseBoolean(br.readLine());
                if (isBotAlreadyInTheNetwork) {
                    Random random = new Random();
                    identity.setId(random.nextInt(UPPER_ID_LIMIT));
                }
            } catch (IOException e) {
                Logger.red("It was not possible to retrieve the response from the server");
                return 0;
            }
            i++;
        }

        if (i > UPPER_ID_LIMIT) {
            System.exit(-1);
        }

        BotUtilities.closeConnection(connection);

        connection =
                BotUtilities.buildConnection("POST", "http://" +
                        Variables.HOST + ":" + Variables.PORT + "/admin/join");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        OutputStream os;
        try {
            os = connection.getOutputStream();
        } catch (IOException e) {
            Logger.red("There was an error during output stream creation");
            return 0;
        }

        String json = "";
        try {
            json = mapper.writeValueAsString(identity);
        } catch (IOException e) {
            Logger.red("There was an error while generating the json string");
            return 0;
        }

        byte[] input = null;
        try {
            input = json.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.red("There was a problem while turning the string into bytes");
            return 0;
        }

        try {
            os.write(input, 0, input.length);
        } catch (IOException e) {
            Logger.red("There was an error while writing on output stream");
            return 0;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
        } catch (IOException e) {
            Logger.red("It was not possible to initialize the BufferedReader");
            return 0;
        }

        try {
            String[] responseLine = br.readLine().split("-");
            identity.setPosition(new ObjectMapper().readValue(responseLine[0], Position.class));
            List<BotIdentity> robots = new ObjectMapper().readValue(responseLine[1], new TypeReference<List<BotIdentity>>() {
            });
            otherBots.addAll(robots);
        } catch (IOException e) {
            Logger.red("It was not possible to retrieve the response from the server");
            return 0;
        }

        BotUtilities.closeConnection(connection);
        otherBots.removeElement(identity);

        district = BotUtilities.districtCalculator(identity.getPosition());

        if (otherBots.isEmpty()) {
            return -1;
        }
        Logger.cyan("Letting my presence known");
        AtomicCounter counter = new AtomicCounter(otherBots.size());
        List<BotIdentity> nonRespondingRobots = new ArrayList<>();

        otherBots.getCopy().forEach(botIdentity -> {

            if (BOT_THREAD_DEBUGGING) {
                try {
                    Logger.blue("DO SOMETHING NOW");
                    sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            CommPair commPair = BotUtilities.retrieveCommunicationPair(botIdentity);

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

            commPair.getCommunicationStub().joinRequestGRPC(identikit, new StreamObserver<BotGRPC.Acknowledgement>() {
                @Override
                public void onNext(BotGRPC.Acknowledgement value) {
                    counter.decrement();
                    if (!value.getAck()) {
                        Logger.purple("robot " + botIdentity + " didn't add me to its network");
                    }
                }

                @Override
                public void onError(Throwable t) {
                    Logger.red("Robot " + botIdentity.getId() + " did not reply to my joinRequest call");
                    counter.decrement();
                    if (t.getClass() == StatusRuntimeException.class) {
                        nonRespondingRobots.add(botIdentity);
                    }
                    checkCounter(nonRespondingRobots, counter);
                }

                @Override
                public void onCompleted() {
                    checkCounter(nonRespondingRobots, counter);
                }
            });
        });

        return 1;
    }

    public synchronized void checkCounter(List<BotIdentity> nonRespondingRobots, AtomicCounter counter) {
        if (counter.getCounter() == 0) {
            Logger.green("Hello procedure completed");
            counter.add(10);
            notify();
            if (!nonRespondingRobots.isEmpty()) {
                Logger.yellow("Starting the eliminator thread to delete " + nonRespondingRobots.size() + " robots");
                EliminatorThread eliminatorThread = new EliminatorThread(nonRespondingRobots, false);
                eliminatorThread.start();
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

    public PollutionSensorThread getPollutionSensorThread() {
        return pollutionSensorThread;
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
        Logger.blue("Closing the communication channel for " + deadRobots.size() + " robots");
        for (BotIdentity deadRobot : deadRobots) {
            Logger.whiteDebuggingPrint(deadRobot.toString(), BOT_THREAD_DEBUGGING);
            removalOperation &= otherBots.removeElement(deadRobot);
            CommPair communicationPair = openComms.removePair(deadRobot);
            if (communicationPair != null) {
                try {
                    communicationPair.getManagedChannel().shutdown().awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Logger.red("Unable to close the channel the right way, forcing it closed now");
                    communicationPair.getManagedChannel().shutdownNow();
                }
            }
        }
        return removalOperation;
    }

    public CommPair newCommunicationChannel(BotIdentity destination, ManagedChannel channel, BotServicesGrpc.BotServicesStub stub) {
        CommPair commPair = new CommPair(channel, stub);
        openComms.addPair(destination, commPair);
        return commPair;
    }

    public void changeMyPosition(int district) {
        Position newPosition = BotUtilities.positionCalculator(district);
        BotIdentity tmp = identity;
        identity.setPosition(newPosition);
        setDistrict(district);
        Logger.cyan("My new district is > " + district + " > My new position is > " + newPosition);

        ObjectMapper mapper = new ObjectMapper();

        HttpURLConnection connection =
                BotUtilities.buildConnection("PUT", "http://" +
                        Variables.HOST + ":" + Variables.PORT + "/admin/update");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        OutputStream os = null;
        try {
            os = connection.getOutputStream();
        } catch (IOException e) {
            Logger.red("There was an error during output stream creation");
        }

        String json = "";
        try {
            json = "[" + mapper.writeValueAsString(tmp) + ", " + mapper.writeValueAsString(identity) + "]";
        } catch (IOException e) {
            Logger.red("There was an error while generating the json string");
        }

        byte[] input = null;
        try {
            input = json.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.red("There was a problem while turning the string into bytes");
        }

        try {
            os.write(input, 0, input.length);
        } catch (IOException e) {
            Logger.red("There was an error while writing on output stream");
        }

        BotUtilities.closeConnection(connection);

        List<BotIdentity> nonRespondingBots = new ArrayList<>();
        List<BotIdentity> fleetSnapshot = otherBots.getCopy();
        AtomicCounter counter = new AtomicCounter(fleetSnapshot.size());

        for (BotIdentity botIdentity : fleetSnapshot) {

            CommPair communicationPair = BotUtilities.retrieveCommunicationPair(botIdentity);

            BotGRPC.BotInformation botInfo = BotGRPC.BotInformation.newBuilder()
                    .setId(identity.getId())
                    .setHost(identity.getIp())
                    .setPort(identity.getPort())
                    .setPosition(BotGRPC.Position.newBuilder()
                            .setX(newPosition.getX())
                            .setY(newPosition.getY())
                            .build())
                    .build();

            communicationPair.getCommunicationStub()
                    .positionModificationRequestGRPC(botInfo, new StreamObserver<BotGRPC.Acknowledgement>() {

                        @Override
                        public void onNext(BotGRPC.Acknowledgement value) {
                            counter.decrement();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Logger.red("Robot " + botIdentity.getId() + " did not reply");
                            counter.decrement();
                            nonRespondingBots.add(botIdentity);
                            checkAndWakeup(nonRespondingBots, counter);
                        }

                        @Override
                        public void onCompleted() {
                            checkAndWakeup(nonRespondingBots, counter);
                        }
                    });
        }

        pollutionSensorThread.closeConnection(district);
    }

    private void checkAndWakeup(List<BotIdentity> deadRobots, AtomicCounter counter) {
        synchronized (this) {
            if (counter.getCounter() == 0) {
                counter.add(10);
                synchronized (botServices) {
                    botServices.notify();
                }
                if(!deadRobots.isEmpty()) {
                    EliminatorThread eliminatorThread = new EliminatorThread(deadRobots, false);
                    eliminatorThread.start();
                }
            }
        }
    }
}