package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.service.BotServices;
import extra.Logger.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

//TODO
//>> FLAVOUR :: DEBUGGING-ARANCIONE <<
//CAPIRE PERCHÈ A VOLTE IL PROCESSO DI AVVIAMENTO DEL SERVER GRPC SI PIANTA

/**
 * GrpcServicesThread is a thread that handles incoming communications from other threads.
 */
public class GrpcServicesThread extends Thread {
    private final BotServices grpcServices;
    private int port;

    /**
     * @see BotThread
     * Custom constructor that starts the communication server up.
     * @param port
     * @param botServices
     */
    public GrpcServicesThread(int port, BotServices botServices) {
        this.port = port;

        grpcServices = botServices;
    }

    /**
     * Override of the run method that starts the server up
     */
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
