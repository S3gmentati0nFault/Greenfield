package cleaningBot;

import extra.AtomicCounter.AtomicCounter;
import utilities.Variables;
import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import extra.Logger.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.map.ObjectMapper;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

import static utilities.Variables.DEBUGGING;
import static utilities.Variables.NUMBER_OF_DISTRICTS;

public class BotUtilities {
    private static AtomicCounter counter;

    public static void botRemovalFunction(BotIdentity identity, boolean quitting) {
        List<BotIdentity> temp = new ArrayList<>();
        temp.add(identity);
        botRemovalFunction(temp, quitting);
    }

//    TODO
//    >> FLAVOUR :: EFFICIENZA-ARANCIO <<
//    PASSARE A UN SISTEMA A LISTE DI MODO DA NON REPLICARE IL PROCESSO DI STABILIZZAZIONE PER OGNI ROBOT DA ELIMINARE
    public static boolean botRemovalFunction(List<BotIdentity> deadRobots, boolean quitting) {
        ObjectMapper mapper = new ObjectMapper();

        BotThread.getInstance().removeBot(deadRobots);

        HttpURLConnection connection = buildConnection("DELETE", "http://" +
                    Variables.HOST+":" +
                    Variables.PORT +
                    "/admin/remove");

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
            json = mapper.writeValueAsString(deadRobots);
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

        closeConnection(connection);

        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
        int currentSize = fleetSnapshot.size();
        if(!fleetSnapshot.isEmpty()) {
            counter = new AtomicCounter(currentSize);

            for (BotIdentity deadRobot : deadRobots) {
                fleetSnapshot.forEach(botIdentity -> {
                    CommPair openComm = BotThread.getInstance().getOpenComms().getValue(botIdentity);
                    ManagedChannel channel;
                    if (openComm != null) {
                        channel = openComm.getManagedChannel();
                    } else {
                        channel = ManagedChannelBuilder
                                .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                                .usePlaintext()
                                .build();
                    }
                    BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);
                    BotThread.getInstance().newCommunicationChannel(botIdentity, channel, serviceStub);


                    BotGRPC.BotInformation identikit = BotGRPC.BotInformation
                            .newBuilder()
                            .setId(deadRobot.getId())
                            .setPort(deadRobot.getPort())
                            .setHost(deadRobot.getIp())
                            .setPosition(BotGRPC.Position.newBuilder()
                                    .setX(deadRobot.getPosition().getX())
                                    .setY(deadRobot.getPosition().getY())
                                    .build()
                            )
                            .build();

                    serviceStub.crashNotificationGRPC(identikit, new StreamObserver<BotGRPC.IntegerValue>() {
                        @Override
                        public void onNext(BotGRPC.IntegerValue returnMessage) {
                            counter.decrement();
                            if (returnMessage.getValue() == -1) {
                                Logger.yellow("The robot has already been removed from the system");
                            } else {
                                Logger.green("The robot has been correctly removed!");
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            Logger.red("robot " + botIdentity + " sent error " + t.getClass());
                        }

                        @Override
                        public void onCompleted() {
                            checkCounter(quitting);
                        }
                    });
                });
            }
        }

        if(quitting) {
            System.exit(0);
        }

        else {
            fleetSnapshot.add(BotThread.getInstance().getIdentity());
            currentSize++;

            if(DEBUGGING) {
                System.out.println("DIMENSIONE DELLA LISTA POST ELIMINAZIONE -> " + fleetSnapshot.size());
            }

            List<Queue<BotIdentity>> districtDistribution = distributionCalculator(fleetSnapshot);

            if(DEBUGGING) {
                System.out.println("SITUAZIONE INIZIALE");
                for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                    System.out.println("DISTRICT " + (i + 1) + " < " + districtDistribution.get(i).size() + " >");
                    for (BotIdentity botIdentity : districtDistribution.get(i)) {
                        System.out.println(botIdentity);
                    }
                }
            }

            int limit = (currentSize / 4);
            if(currentSize % 4 > 1) {
                limit += 1;
            }
            else{
                limit += currentSize % 4;
            }
            int reducedLimit = (currentSize / 4);

            if(DEBUGGING) {
                System.out.println("LIMIT -> " + limit);
            }

            for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                while(districtDistribution.get(i).size() > limit) {
                    moveBotsAround(districtDistribution, limit, i, reducedLimit);
                }
            }

            if(DEBUGGING) {
                System.out.println("TERMINE DEL PROCESSO");
                for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                    System.out.println("DISTRICT " + (i + 1) + " < " + districtDistribution.get(i).size() + " >");
                    for (BotIdentity botIdentity : districtDistribution.get(i)) {
                        System.out.println(botIdentity);
                    }
                }
            }
        }

        return true;
    }

    private static void moveBotsAround(List<Queue<BotIdentity>> distribution, int limit, int overpopulatedDistrict, int reducedLimit) {

        if(DEBUGGING) {
            System.out.println("MOVING SOME ROBOTS AWAY FROM " + (overpopulatedDistrict + 1));
        }

        int receivingDistrict = 0;
        int min = distribution.get(0).size();
        for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            if(DEBUGGING) {
                System.out.println("Possible district -> "  + i + " its distribution: " + distribution.get(i).size());
            }
            if(distribution.get(i).size() < min && distribution.get(i).size() < limit) {
                receivingDistrict = i;
                min = distribution.get(i).size();
            }
        }

        if(DEBUGGING) {
            System.out.println("RECEIVING DISTRICT -> " + (receivingDistrict + 1));
        }

        while(distribution.get(receivingDistrict).size() <= reducedLimit &&
                distribution.get(overpopulatedDistrict).size() > limit) {

            BotIdentity botToBeMoved = null;
            botToBeMoved = distribution.get(overpopulatedDistrict).poll();
            if(DEBUGGING) {
                System.out.println("Moving " + botToBeMoved + " to " + (receivingDistrict + 1));
            }

            distribution.get(receivingDistrict).add(botToBeMoved);

            if(botToBeMoved == BotThread.getInstance().getIdentity()) {
                if(DEBUGGING) {
                    System.out.println("MOVING MYSELF");
                }
                BotThread.getInstance().changeMyPosition(receivingDistrict + 1);
            }
            else{
                ManagedChannel channel;
                BotServicesGrpc.BotServicesStub serviceStub;

                CommPair communicationPair = BotThread.getInstance().getOpenComms().getValue(botToBeMoved);
                if(communicationPair == null) {
                    channel = ManagedChannelBuilder
                            .forTarget(botToBeMoved.getIp() + ":" + botToBeMoved.getPort())
                            .usePlaintext()
                            .build();

                    serviceStub = BotServicesGrpc.newStub(channel);
                    BotThread.getInstance().newCommunicationChannel(botToBeMoved, channel, serviceStub);
                }
                else {
                    channel = communicationPair.getManagedChannel();
                    serviceStub = communicationPair.getCommunicationStub();
                }
                BotGRPC.IntegerValue district = BotGRPC.IntegerValue.newBuilder().setValue(receivingDistrict + 1).build();
                serviceStub.moveRequestGRPC(district, new StreamObserver<BotGRPC.Acknowledgement>() {
                    @Override
                    public void onNext(BotGRPC.Acknowledgement value) {

                    }

                    @Override
                    public void onError(Throwable t) {
                        Logger.red("Something went wrong " + t.getMessage());
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
            }
        }
    }

    private static void checkCounter(boolean quitting) {
        if(counter.getCounter() == 0) {
            if(quitting) {
                System.exit(0);
            }
        }
    }

    /**
     * Method that builds a new connection towards a certain host
     * @param requestMethod The HTTP request method
     * @param url destination URL
     * @return HttpURLConnection object used to send data in the startNewBot() procedure.
     */
    public static HttpURLConnection buildConnection(String requestMethod, String url) {
        URL requestURL;

        try{
            requestURL = new URL(url);
        }catch(MalformedURLException e){
            Logger.red("The url was malformed");
            return null;
        }

        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) requestURL.openConnection();
        }
        catch(IOException e){
            Logger.red("There was an error during connection opening procedure");
            return null;
        }

        try{
            connection.setRequestMethod(requestMethod);
        }catch(ProtocolException e){
            Logger.red("There was an error during request method selection");
            return null;
        }

        return connection;
    }

    /**
     * Method used to close an open connection
     */
    public static void closeConnection(HttpURLConnection connection) {
        try{
            if(connection.getResponseCode() == 200) {
                Logger.green("The request " + connection.getRequestMethod() + " went fine");
            }
            else{
                Logger.yellow("The response code was > " + connection.getResponseCode());
            }
        }catch(Exception e){
            Logger.red("Something went wrong while retrieving the response code");
        }

        Logger.yellow("Closing the connection channel");
        connection.disconnect();
    }

    public static int districtCalculator(Position position) {
        if(position.getY() < 5) {
            if(position.getX() < 5) {
                return 1;
            }
            else{
                return 2;
            }
        }
        else{
            if(position.getX() < 5){
                return 4;
            }
            else{
                return 3;
            }
        }
    }

    public static Position positionCalculator(int district) {
        Position position = null;
        Random random = new Random();
        switch(district) {
            case 1:
                position = new Position(random.nextInt(5), random.nextInt(5));
                break;
            case 2:
                position = new Position(random.nextInt(5) + 5, random.nextInt(5));
                break;
            case 3:
                position = new Position(random.nextInt(5) + 5, random.nextInt(5) + 5);
                break;
            case 4:
                position = new Position(random.nextInt(5), random.nextInt(5) + 5);
                break;
        }
        return position;
    }

    public static List<Queue<BotIdentity>> distributionCalculator(List<BotIdentity> fleetSnapshot) {
        BotIdentityComparator comparator = new BotIdentityComparator();
            List<Queue<BotIdentity>> districtDistribution = new ArrayList<>();
            for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                districtDistribution.add(new PriorityQueue<>(comparator));
            }

            for (BotIdentity botIdentity : fleetSnapshot) {
                switch(districtCalculator(botIdentity.getPosition())) {
                    case 1:
                        districtDistribution.get(0).add(botIdentity);
                        break;
                    case 2:
                        districtDistribution.get(1).add(botIdentity);
                        break;
                    case 3:
                        districtDistribution.get(2).add(botIdentity);
                        break;
                    case 4:
                        districtDistribution.get(3).add(botIdentity);
                        break;
                }
            }
            return districtDistribution;
    }
}
