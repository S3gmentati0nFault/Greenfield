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

    /**
     * Method that builds a new connection towards a certain host
     *
     * @param requestMethod The HTTP request method
     * @param url           destination URL
     * @return HttpURLConnection object used to send data in the startNewBot() procedure.
     */
    public static HttpURLConnection buildConnection(String requestMethod, String url) {
        URL requestURL;

        try {
            requestURL = new URL(url);
        } catch (MalformedURLException e) {
            Logger.red("The url was malformed");
            return null;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestURL.openConnection();
        } catch (IOException e) {
            Logger.red("There was an error during connection opening procedure");
            return null;
        }

        try {
            connection.setRequestMethod(requestMethod);
        } catch (ProtocolException e) {
            Logger.red("There was an error during request method selection");
            return null;
        }

        return connection;
    }

    /**
     * Method used to close an open connection
     */
    public static void closeConnection(HttpURLConnection connection) {
        try {
            if (connection.getResponseCode() == 200) {
                Logger.green("The request " + connection.getRequestMethod() + " went fine");
            } else {
                Logger.blue("The response code was > " + connection.getResponseCode());
            }
        } catch (Exception e) {
            Logger.red("Something went wrong while retrieving the response code");
        }

        Logger.purple("Closing the connection channel");
        connection.disconnect();
    }

    public static int districtCalculator(Position position) {
        if (position.getY() < 5) {
            if (position.getX() < 5) {
                return 1;
            } else {
                return 2;
            }
        } else {
            if (position.getX() < 5) {
                return 4;
            } else {
                return 3;
            }
        }
    }

    public static Position positionCalculator(int district) {
        Position position = null;
        Random random = new Random();
        switch (district) {
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
        for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            districtDistribution.add(new PriorityQueue<>(comparator));
        }

        for (BotIdentity botIdentity : fleetSnapshot) {
            switch (districtCalculator(botIdentity.getPosition())) {
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

    public static CommPair retrieveCommunicationPair(BotIdentity botIdentity) {
        CommPair communicationPair = BotThread.getInstance().getOpenComms().getValue(botIdentity);
        if (communicationPair != null) {
            return communicationPair;
        }

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                .usePlaintext()
                .build();
        BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

        return BotThread.getInstance().newCommunicationChannel(botIdentity, channel, serviceStub);
    }
}
