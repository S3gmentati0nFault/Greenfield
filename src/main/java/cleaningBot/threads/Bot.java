package cleaningBot.threads;

import extra.Logger.Logger;
import extra.Position.Position;
import extra.CustomRandom.CustomRandom;
import extra.Variables;
import beans.BotIdentity;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class Bot extends Thread{
    private Position position;
    private List<BotIdentity> otherBots;
    private BotIdentity identity;

    public Bot(){
        identity = new BotIdentity(CustomRandom.getInstance().rnInt(100),
                CustomRandom.getInstance().rnInt(65534),
                "localhost");
    }

    public void run(){
        GrpcServicesThread grpcThread = new GrpcServicesThread(identity, this);
        grpcThread.start();

        if(!startNewBot()){
            Logger.red("There was an error during Thread instantiation");
        }
    }

    private boolean startNewBot() {
        URL requestURL;
        ObjectMapper mapper = new ObjectMapper();

        try{
            requestURL = new URL("http://" +
                    Variables.HOST+":" +
                    Variables.PORT +
                    "/admin/join");
        }catch(MalformedURLException e){
            Logger.red("The url was malformed");
            return false;
        }

        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) requestURL.openConnection();
        }
        catch(IOException e){
            Logger.red("There was an error during connection opening procedure");
            return false;
        }

        try{
            connection.setRequestMethod("POST");
        }catch(ProtocolException e){
            Logger.red("There was an error during request method selection");
            return false;
        }

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
            String[] responseLine = br.readLine().toString().split("-");
            position = new ObjectMapper().readValue(responseLine[0], Position.class);
            otherBots = new ObjectMapper().readValue(responseLine[1],
                    new TypeReference<List<BotIdentity>>(){});
        } catch (IOException e) {
            Logger.red("It was not possible to retrieve the response from the server");
            return false;
        }

        if(!otherBots.isEmpty()){
            Logger.cyan("Contacting the other bots");
            otherBots.forEach(botIdentity -> {
                if(botIdentity.equals(identity)){
                    return;
                }
                ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();

                BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);
                BotGRPC.BotNetworkingInformations identikit = BotGRPC.BotNetworkingInformations
                        .newBuilder()
                        .setId(identity.getId())
                        .setPort(identity.getPort())
                        .setHost(identity.getIp())
                        .build();
                serviceStub.joinAdvertiseGRPC(identikit, new StreamObserver<BotGRPC.Acknowledgement>() {
                    @Override
                    public void onNext(BotGRPC.Acknowledgement value) {
                        if(!value.getAck()){
                            Logger.purple("robot " + botIdentity + " didn't add me to its network");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Logger.red("robot " + botIdentity + " sent an error");
                    }

                    @Override
                    public void onCompleted() {otherBots.forEach(
                            botIdentity -> {System.out.println(botIdentity);}
                    );}
                });
            });
        }

        return true;
    }

    public List<BotIdentity> getOtherBots() {
        return otherBots;
    }
}
