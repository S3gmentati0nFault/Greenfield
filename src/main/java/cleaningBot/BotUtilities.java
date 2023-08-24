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

public class BotUtilities {
    private static AtomicCounter counter;

    public static void botRemovalFunction(BotIdentity identity, boolean quitting) {
        List<BotIdentity> temp = new ArrayList<>();
        temp.add(identity);
        botRemovalFunction(temp, quitting);
    }

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

            for (BotIdentity botIdentity : fleetSnapshot) {
                System.out.println(botIdentity);
            }

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

            System.out.println("DIMENSIONE DELLA LISTA POST ELIMINAZIONE -> " + fleetSnapshot.size());

//            TODO
//            >> FLAVOUR :: STILE-ROSSO <<
//            CAPIRE COME FUNZIONANO LE LISTE DI LISTE E CAMBIARE QUESTO SCHIFO
            int[] distributions = new int[4];
            BotIdentityComparator comparator = new BotIdentityComparator();
            Queue<BotIdentity> district1 = new PriorityQueue<>(comparator);
            Queue<BotIdentity> district2 = new PriorityQueue<>(comparator);
            Queue<BotIdentity> district3 = new PriorityQueue<>(comparator);
            Queue<BotIdentity> district4 = new PriorityQueue<>(comparator);


            for (BotIdentity botIdentity : fleetSnapshot) {
                switch(districtCalculator(botIdentity.getPosition())) {
                    case 1:
                        district1.add(botIdentity);
                        distributions[0]++;
                        break;
                    case 2:
                        district2.add(botIdentity);
                        distributions[1]++;
                        break;
                    case 3:
                        district3.add(botIdentity);
                        distributions[2]++;
                        break;
                    case 4:
                        district4.add(botIdentity);
                        distributions[3]++;
                        break;
                }
            }


            System.out.println("SITUAZIONE INIZIALE");
            for(int i = 0; i < 4; i++) {
                System.out.println("District " + (i + 1) + " -> " + distributions[i]);
            }

            System.out.println("DISTRICT 1");
            district1.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 2");
            district2.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 3");
            district3.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 4");
            district4.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            int limit = (currentSize / 4) + (currentSize % 4);
            int reducedLimit = (currentSize / 4);

            System.out.println("LIMIT -> " + limit);

//            TODO
//            >> FLAVOUR :: LOGICA-GIALLO <<
//            PUNTARE A STABILIZZARE MAGGIORMENTE LA DISTRIBUZIONE DI MODO CHE NON VI SIANO DEI DISTRETTI CON 3 ROBOT E
//            ALTRI CHE NE SONO COMPLETAMENTE PRIVI
            for(int i = 0; i < 4; i++) {
                while(distributions[i] > limit) {
                    moveBotsAround(district1, district2, district3, district4,
                            distributions, limit, i, reducedLimit);
                }
            }

            System.out.println("TERMINE DEL PROCESSO");

            for(int i = 0; i < 4; i++) {
                System.out.println("District " + (i + 1) + " -> " + distributions[i]);
            }

            System.out.println("DISTRICT 1");
            district1.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 2");
            district2.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 3");
            district3.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });

            System.out.println("DISTRICT 4");
            district4.forEach(botIdentity -> {
                System.out.println(botIdentity);
            });
        }

        return true;
    }

    private static void moveBotsAround(Queue<BotIdentity> district1, Queue<BotIdentity> district2,
                                       Queue<BotIdentity> district3, Queue<BotIdentity> district4,
                                       int[] distributions, int limit, int overpopulatedDistrict,
                                       int reducedLimit) {

        System.out.println("MOVING SOME ROBOTS AWAY FROM " + (overpopulatedDistrict + 1));

        int receivingDistrict = 0;
        int min = distributions[0];
        for(int i = 0; i < 4; i++) {
            System.out.println("Possible district -> "  + i + " its distribution: " + distributions[i]);
            if(distributions[i] < min && distributions[i] < limit) {
                receivingDistrict = i;
                min = distributions[i];
            }
        }

        System.out.println("RECEIVING DISTRICT -> " + (receivingDistrict + 1));

        while(distributions[receivingDistrict] < reducedLimit && distributions[overpopulatedDistrict] > limit) {

            BotIdentity botToBeMoved = null;

            switch(overpopulatedDistrict) {
                case 0:
                    botToBeMoved = district1.poll();
                    System.out.println("Moving " + botToBeMoved + " to " + (receivingDistrict + 1));
                    break;
                case 1:
                    botToBeMoved = district2.poll();
                    System.out.println("Moving " + botToBeMoved + " to " + receivingDistrict);
                    break;
                case 2:
                    botToBeMoved = district3.poll();
                    System.out.println("Moving " + botToBeMoved + " to " + receivingDistrict);
                    break;
                case 3:
                    botToBeMoved = district4.poll();
                    System.out.println("Moving " + botToBeMoved + " to " + receivingDistrict);
                    break;
            }

            Random random = new Random();

            switch(receivingDistrict) {
                case -1:
                    Logger.red("Something has gone terribly wrong...");
                    break;
                case 0:
                    botToBeMoved.setPosition(new Position(random.nextInt(5), random.nextInt(5)));
                    district1.add(botToBeMoved);
                    break;
                case 1:
                    botToBeMoved.setPosition(new Position(random.nextInt(5) + 5, random.nextInt(5)));
                    district2.add(botToBeMoved);
                    break;
                case 2:
                    botToBeMoved.setPosition(new Position(random.nextInt(5) + 5, random.nextInt(5) + 5));
                    district3.add(botToBeMoved);
                    break;
                case 3:
                    botToBeMoved.setPosition(new Position(random.nextInt(5), random.nextInt(5) + 5));
                    district4.add(botToBeMoved);
                    break;
            }

            distributions[overpopulatedDistrict]--;
            distributions[receivingDistrict]++;

            if(botToBeMoved == BotThread.getInstance().getIdentity()) {
                System.out.println("MOVING MYSELF");
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
}
