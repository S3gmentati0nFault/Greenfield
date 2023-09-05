package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.CommPair;
import extra.AtomicCounter.AtomicCounter;
import extra.Logger.Logger;
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.map.ObjectMapper;
import services.grpc.BotGRPC;
import utilities.Variables;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static cleaningBot.BotUtilities.*;
import static utilities.Variables.*;

public class EliminatorThread extends Thread {

    private List<BotIdentity> deadRobots;
    private boolean quitting;

    public EliminatorThread(List<BotIdentity> deadRobots, boolean quitting) {
        this.deadRobots = deadRobots;
        this.quitting = quitting;
    }

    public EliminatorThread(BotIdentity deadRobot, boolean quitting) {
        this.deadRobots = new ArrayList<>();
        deadRobots.add(deadRobot);
        this.quitting = quitting;
    }

    @Override
    public void run() {
        botRemovalFunction();
    }

    public boolean botRemovalFunction() {
        ObjectMapper mapper = new ObjectMapper();

        BotThread.getInstance().removeBot(deadRobots);

        HttpURLConnection connection = buildConnection("DELETE", "http://" +
                Variables.HOST + ":" +
                Variables.PORT +
                "/admin/remove");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        OutputStream os;
        try {
            os = connection.getOutputStream();
        } catch (IOException e) {
            Logger.red("There was an error during output stream creation");
            return false;
        }

        String json = "";
        try {
            json = mapper.writeValueAsString(deadRobots);
        } catch (IOException e) {
            Logger.red("There was an error while generating the json string");
            return false;
        }

        byte[] input = null;
        try {
            input = json.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.red("There was a problem while turning the string into bytes");
            return false;
        }

        try {
            os.write(input, 0, input.length);
        } catch (IOException e) {
            Logger.red("There was an error while writing on output stream");
            return false;
        }

        closeConnection(connection);

        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
        int currentSize = fleetSnapshot.size();

        if (fleetSnapshot.isEmpty()) {
            if (quitting) {
                System.exit(0);
            }
            return true;
        }

        AtomicCounter counter = new AtomicCounter(currentSize);

        List<BotGRPC.BotInformation> informations = new ArrayList<>();
        for (BotIdentity deadRobot : deadRobots) {
            informations.add(BotGRPC.BotInformation.newBuilder()
                    .setId(deadRobot.getId())
                    .setPort(deadRobot.getPort())
                    .setHost(deadRobot.getIp())
                    .setPosition(BotGRPC.Position.newBuilder()
                            .setX(deadRobot.getPosition().getX())
                            .setY(deadRobot.getPosition().getY())
                            .build()
                    )
                    .build()
            );
        }

        fleetSnapshot.forEach(botIdentity -> {

            CommPair communicationPair = retrieveCommunicationPair(botIdentity);

            BotGRPC.DeadRobotList deadRobots = BotGRPC.DeadRobotList.newBuilder()
                    .addAllDeadRobots(informations)
                    .build();

            communicationPair
                    .getCommunicationStub()
                    .crashNotificationGRPC(deadRobots, new StreamObserver<BotGRPC.IntegerValue>() {
                        @Override
                        public void onNext(BotGRPC.IntegerValue returnMessage) {
                            counter.decrement();
                            if (returnMessage.getValue() == -1) {
                                Logger.purple("The robot has already been removed from the system");
                            } else {
                                Logger.green("The robot has been correctly removed!");
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            Logger.red("robot " + botIdentity.getId() + " didn't reply to my CrashNotification call");
                        }

                        @Override
                        public void onCompleted() {
                            checkCounter(quitting, counter);
                        }
                    });
        });

        if (quitting) {
            System.exit(0);
        }

        fleetSnapshot.add(BotThread.getInstance().getIdentity());
        currentSize++;

        Logger.whiteDebuggingPrint("DIMENSIONE DELLA LISTA POST ELIMINAZIONE -> " + fleetSnapshot.size(), ELIMINATOR_THREAD_DEBUGGING);

        List<Queue<BotIdentity>> districtDistribution = distributionCalculator(fleetSnapshot);

        if (ELIMINATOR_THREAD_DEBUGGING) {
            System.out.println("SITUAZIONE INIZIALE");
            for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                System.out.println("DISTRICT " + (i + 1) + " < " + districtDistribution.get(i).size() + " >");
                for (BotIdentity botIdentity : districtDistribution.get(i)) {
                    System.out.println(botIdentity);
                }
            }
        }

        int limit = (currentSize / 4) + Math.min(currentSize % 4, 1);
        int reducedLimit = (currentSize / 4);

        Logger.whiteDebuggingPrint("LIMIT -> " + limit, ELIMINATOR_THREAD_DEBUGGING);

        for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            while (districtDistribution.get(i).size() > limit) {
                moveBotsAround(districtDistribution, limit, i, reducedLimit);
            }
        }

        if (ELIMINATOR_THREAD_DEBUGGING) {
            System.out.println("TERMINE DEL PROCESSO");
            for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                System.out.println("DISTRICT " + (i + 1) + " < " + districtDistribution.get(i).size() + " >");
                for (BotIdentity botIdentity : districtDistribution.get(i)) {
                    System.out.println(botIdentity);
                }
            }
        }

        return true;
    }

    private static void moveBotsAround(List<Queue<BotIdentity>> distribution, int limit, int overpopulatedDistrict, int reducedLimit) {

        Logger.whiteDebuggingPrint("MOVING SOME ROBOTS AWAY FROM "
                + (overpopulatedDistrict + 1), ELIMINATOR_THREAD_DEBUGGING);

        int receivingDistrict = 0;
        int min = distribution.get(0).size();
        for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            Logger.whiteDebuggingPrint("Possible district -> "
                            + i + " its distribution: "
                            + distribution.get(i).size(),
                    ELIMINATOR_THREAD_DEBUGGING);

            if (distribution.get(i).size() < min && distribution.get(i).size() < limit) {
                receivingDistrict = i;
                min = distribution.get(i).size();
            }
        }

        Logger.whiteDebuggingPrint("RECEIVING DISTRICT -> " + (receivingDistrict + 1), ELIMINATOR_THREAD_DEBUGGING);

        if (receivingDistrict == overpopulatedDistrict) {
            return;
        }

        while (distribution.get(receivingDistrict).size() <= reducedLimit &&
                distribution.get(overpopulatedDistrict).size() > limit) {

            final BotIdentity botToBeMoved = distribution.get(overpopulatedDistrict).poll();

            Logger.whiteDebuggingPrint("Moving " + botToBeMoved
                    + " to " + (receivingDistrict + 1), ELIMINATOR_THREAD_DEBUGGING);

            distribution.get(receivingDistrict).add(botToBeMoved);

            if (botToBeMoved == BotThread.getInstance().getIdentity()) {
                Logger.whiteDebuggingPrint("MOVING MYSELF", ELIMINATOR_THREAD_DEBUGGING);
                BotThread.getInstance().changeMyPosition(receivingDistrict + 1);
            } else {
                CommPair communicationPair = retrieveCommunicationPair(botToBeMoved);
                BotGRPC.IntegerValue district = BotGRPC.IntegerValue.newBuilder().setValue(receivingDistrict + 1).build();

                communicationPair
                        .getCommunicationStub()
                        .moveRequestGRPC(district, new StreamObserver<BotGRPC.Acknowledgement>() {
                            @Override
                            public void onNext(BotGRPC.Acknowledgement value) {

                            }

                            @Override
                            public void onError(Throwable t) {
                                Logger.red("Robot " + botToBeMoved.getId() + " did not reply to my moveRequest call");
                            }

                            @Override
                            public void onCompleted() {

                            }
                        });
            }
        }
    }

    private static void checkCounter(boolean quitting, AtomicCounter counter) {
        if (counter.getCounter() == 0) {
            counter.add(10);
            if (quitting) {
                System.exit(0);
            }
        }
    }
}
