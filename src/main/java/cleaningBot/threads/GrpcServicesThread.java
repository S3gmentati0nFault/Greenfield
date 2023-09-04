package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.service.BotServices;
import extra.Logger.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

import static utilities.Variables.DEBUGGING;

/**
 * GrpcServicesThread is a thread that handles incoming communications from other threads.
 */
public class GrpcServicesThread extends Thread {
    private final BotServices grpcServices;
    private int port;
    private Server server;
    private boolean running;

    /**
     * @param port
     * @param botServices
     * @see BotThread
     * Custom constructor that starts the communication server up.
     */
    public GrpcServicesThread(int port, BotServices botServices) {
        this.port = port;

        grpcServices = botServices;
    }

    /**
     * Override of the run method that starts the server up
     */
    @Override
    public void run() {
        try {
            server = ServerBuilder.forPort(port).addService(grpcServices).build();
            server.start();
            Logger.yellow("Server started");
            running = true;
            synchronized (BotThread.getInstance()) {
                BotThread.getInstance().notify();
            }
            server.awaitTermination();
        } catch (IOException e) {
            Logger.red("The port is already occupied, rebooting the server...");
            running = false;
            synchronized (BotThread.getInstance()) {
                BotThread.getInstance().notify();
            }
            server.shutdownNow();
        } catch (InterruptedException e) {
            Logger.red("There was an error while trying to shutdown the server", e);
        }
    }

    public boolean isRunning() {
        return running;
    }
}
