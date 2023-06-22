package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotServices;
import extra.Logger.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * GrpcServicesThread is a thread that handles incoming communications from other threads.
 */
public class GrpcServices extends Thread {
    private final BotServices grpcServices;
    private int port, id;
    private String ip;
    private BotThread botThread;

    /**
     * @see BotThread
     * Custom constructor that starts the communication server up.
     * @param identity This is the bot identity received from the Bot class.
     * @param botThread This is a reference to the bot object, it is used to keep a synchronized
     *            copy of the Bot list around.
     */
    public GrpcServices(BotIdentity identity, BotThread botThread, BotServices botServices) {
        port = identity.getPort();
        id = identity.getId();
        ip = identity.getIp();
        this.botThread = botThread;

        grpcServices = botServices;
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
}
