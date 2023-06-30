package cleaningBot.threads;

import cleaningBot.BotUtilities;
import cleaningBot.service.BotServices;
import extra.Logger.Logger;
import extra.Position.Position;
import extra.CustomRandom.CustomRandom;
import extra.Variables;
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

/**
 * @see cleaningBot.CleaningBot
 * A thread for the CleaningBot class that handles the initial connection with the
 * administration server
 */
public class BotThread extends Thread{
    private Position position;
    private List<BotIdentity> otherBots;
    private BotIdentity identity;
    private BotServices botServices;
    private long timestamp;
    private int district;

    /**
     * Empty constructor that generates random values for both the id and the
     * port.
     */
    public BotThread(){
        identity = new BotIdentity(CustomRandom.getInstance().rnInt(100),
                CustomRandom.getInstance().rnInt(65534),
                "localhost");
        timestamp = -1;

        botServices = new BotServices(this);
    }

    /**
     * Override of run method that starts the thread used for inter-bot communication and
     * initiates the communication channel with the administration server.
     */
    @Override
    public void run(){
        Logger.yellow("Starting grpc services");
        GrpcServicesThread grpcThread = new GrpcServicesThread(identity, this, botServices);
        grpcThread.start();

        if(!startNewBot()){
            Logger.red("There was an error during Thread instantiation");
        }

        Logger.yellow("Starting input thread");
        InputThread inputThread = new InputThread(this);
        inputThread.start();

        Logger.yellow("Starting maintenance thread");
        MaintenanceThread maintenanceThread = new MaintenanceThread(this, botServices);
        maintenanceThread.start();

        Logger.yellow("Starting the pollution measurement sensor thread");
        PollutionSensorThread pollutionSensorThread = new PollutionSensorThread(district, identity);
        pollutionSensorThread.start();
    }

    /**
     * Method that opens a connection with the administration server and makes its
     * presence known to both the server and the other bots in the network.
     * @return It returns true if the operation went well, false otherwise.
     */
    private boolean startNewBot() {
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
            String[] responseLine = br.readLine().toString().split("-");
            position = new ObjectMapper().readValue(responseLine[0], Position.class);
            otherBots = new ObjectMapper().readValue(responseLine[1],
                    new TypeReference<List<BotIdentity>>(){});
        } catch (IOException e) {
            Logger.red("It was not possible to retrieve the response from the server");
            return false;
        }

        BotUtilities.closeConnection(connection);
        otherBots.remove(identity);

        if(!otherBots.isEmpty()){
            printOtherBots();
            Logger.cyan("Contacting the other bots");
            otherBots.forEach(botIdentity -> {
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
                    BotIdentity receiver = botIdentity;

                    @Override
                    public void onNext(BotGRPC.Acknowledgement value) {
                        if(!value.getAck()){
                            Logger.purple("robot " + botIdentity + " didn't add me to its network");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Logger.red("robot " + botIdentity + " sent error " + t.getClass());
                        if(t.getClass() == StatusRuntimeException.class) {
                            synchronized (this) {
                                Logger.yellow("Removing dead robot from the field");
                                otherBots.remove(receiver);
                                updateOthers(receiver);
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {
                        if(Variables.MODE.equals("DEBUG")) {
                            otherBots.forEach(
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

        if(position.getY() < 5) {
            if(position.getX() < 5) {
                district = 1;
            }
            else{
                district = 2;
            }
        }
        else{
            if(position.getX() < 5){
                district = 4;
            }
            else{
                district = 3;
            }
        }

        return true;
    }

    /**
     * Method used to communicate to the other services in the system that one of the nodes
     * has stopped working.
     * @param deadRobot the identity of the robot that has stopped working
     */
    public boolean updateOthers(BotIdentity deadRobot) {
        ObjectMapper mapper = new ObjectMapper();

        HttpURLConnection connection = BotUtilities.buildConnection("DELETE", "http://" +
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

        BotUtilities.closeConnection(connection);

        return true;
    }

    /**
     * Getter for the bots in the system.
     */
    public List<BotIdentity> getOtherBots() {
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
        for (BotIdentity otherBot : otherBots) {
            System.out.println(otherBot);
        }
    }
}
