package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotServices;
import extra.Logger.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServicesThread extends Thread {
    private int port, id;
    private String ip;
    private BotServices grpcServices;
    private Bot bot;


    public GrpcServicesThread(BotIdentity identity, Bot bot) {
        port = identity.getPort();
        id = identity.getId();
        ip = identity.getIp();
        this.bot = bot;

        grpcServices = new BotServices(bot);
    }

    @Override
    public void run(){
        try{
            Server server = ServerBuilder.forPort(port).addService(grpcServices).build();
            server.start();
            server.awaitTermination();
        } catch (IOException e) {
            Logger.red("There was an error while trying to fire up the grpcServices communication server");
        } catch (InterruptedException e) {
            Logger.red("There was an error while trying to stop the grpcServices communication server");
        }
    }

    public void printQueue(){
        grpcServices
                .getContentionQueue()
                .forEach(botEntry -> {System.out.println(botEntry);}
                );
    }
}
