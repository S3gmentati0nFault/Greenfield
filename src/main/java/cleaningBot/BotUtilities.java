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
import java.util.ArrayList;
import java.util.List;

public class BotUtilities {
    private static AtomicCounter counter;

    public static boolean botRemovalFunction(BotIdentity deadRobot, boolean quitting) {
        ObjectMapper mapper = new ObjectMapper();

        BotThread.getInstance().removeBot(deadRobot);

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
            json = mapper.writeValueAsString(deadRobot);
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

        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots();
        int currentSize = fleetSnapshot.size();
        if(!fleetSnapshot.isEmpty()) {
            counter = new AtomicCounter(currentSize);

            fleetSnapshot.forEach(botIdentity -> {
                CommPair openComm = BotThread.getInstance().getOpenComms().getValue(botIdentity);
                ManagedChannel channel;
                System.out.println(openComm);
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


                BotGRPC.BotNetworkingInformations identikit = BotGRPC.BotNetworkingInformations
                        .newBuilder()
                        .setId(deadRobot.getId())
                        .setPort(deadRobot.getPort())
                        .setHost(deadRobot.getIp())
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

        if(quitting) {
            System.exit(0);
        }
        else {
            if(currentSize < 4) {
                return true;
            }
            int[] deltas = new int[4];
            List<List<BotIdentity>> distributionList = new ArrayList<>();
            int objectiveDistribution = currentSize / 4;
            System.out.println("OBJECTIVE DISTRIBUTION: " + objectiveDistribution);
//            for (BotIdentity botIdentity : fleetSnapshot) {
//                switch(districtCalculator(botIdentity.getPosition())) {
//
//                }
//            }
//            for(int i = 0; i < 4; i++) {
//                deltas[i] = distribution[i] - objectiveDistribution;
//            }

        }

        return true;
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
}
