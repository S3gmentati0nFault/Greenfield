package cleaningBot;

import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import extra.Logger.Logger;
import extra.Variables;
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

public class BotUtilities {
    public static boolean botRemovalFunction(BotIdentity deadRobot, BotThread bot) {
        ObjectMapper mapper = new ObjectMapper();

        bot.getOtherBots().remove(deadRobot);

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

        if(!bot.getOtherBots().isEmpty()){
            Logger.cyan("Contacting the other bots");
            bot.getOtherBots().forEach(botIdentity -> {
                ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();

                BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);
                BotGRPC.BotNetworkingInformations identikit = BotGRPC.BotNetworkingInformations
                        .newBuilder()
                        .setId(deadRobot.getId())
                        .setPort(deadRobot.getPort())
                        .setHost(deadRobot.getIp())
                        .build();
                serviceStub.crashAdvertiseGRPC(identikit, new StreamObserver<BotGRPC.Acknowledgement>() {

                    @Override
                    public void onNext(BotGRPC.Acknowledgement value) {
                        if(!value.getAck()){
                            Logger.yellow("The robot has already been deleted from the system");
                        }
                        else{
                            Logger.yellow("Deletion went fine");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Logger.red("robot " + botIdentity + " sent error " + t.getClass());
                    }

                    @Override
                    public void onCompleted() {
                        if(Variables.MODE.equals("DEBUG")) {
                            bot.getOtherBots().forEach(
                                    botIdentity -> {
                                        System.out.println(botIdentity);
                                    }
                            );
                        }
                        channel.shutdown();
                    }
                });
            });
        }

        return true;
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
}
