package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import cleaningBot.service.BotServices;
import exceptions.AlreadyOnMaintenanceException;
import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Variables;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.util.List;

/**
 * Maintenance class that simulates the error rate of the bots and handles the
 * Mutual-exclusion
 */
public class MaintenanceThread extends Thread {
    private boolean onMaintenance;
    private BotThread botThread;
    private BotServices botServices;
    private int counter;

    /**
     * Generic public constructor
     */
    public MaintenanceThread(BotThread botThread, BotServices botServices) {
        try {
            setOnMaintenance(false);
        } catch (AlreadyOnMaintenanceException e) {
            e.printStackTrace();
        }

        this.botThread = botThread;
        this.botServices = botServices;
    }

    /**
     * Override of the run method that starts the maintenance cycle, which simulates the
     * malfunction of the robots
     */
    @Override
    public void run() {
        maintenanceCycle();
    }

    /**
     * Method that simulates the malfunction of the robots
     */
    public void maintenanceCycle() {
        while(!onMaintenance) {
            if(CustomRandom.getInstance().probability(3)){
                try{
                    Logger.blue("The robot should undergo maintenance");
                    if(Variables.AGRAWALA){
                        Logger.blue("TRY USING THE FIX COMMAND");
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    setOnMaintenance(true);
                    agrawalaProcedure();
                } catch (AlreadyOnMaintenanceException e) {
                    Logger.red("The system is already undergoing maintenance");
                }
            }
            try{
                sleep(5000);
            } catch (Exception e) {
                Logger.red("There was a problem while waking up from sleep");
            }
        }
    }

    /**
     * Method that handles the mutual-exclusion by contacting via GRPC all the bots present
     * in the system at the moment of the malfunction
     */
    public synchronized void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");
        List<BotIdentity> fleetSnapshot = botThread.getOtherBots();

        if(fleetSnapshot.size() == 0){
            maintenanceAccess();
            return;
        }

        counter = fleetSnapshot.size();

        Logger.cyan("Contacting the other bots");
        for (BotIdentity botIdentity : fleetSnapshot) {

            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();

            BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

            long timestamp = System.currentTimeMillis();
            if(Variables.DEBUG) {
                System.out.println(timestamp);
            }
            botThread.setTimestamp(timestamp);
            BotGRPC.Identifier identifier = BotGRPC.Identifier
                    .newBuilder()
                    .setId(botIdentity.getId())
                    .setTimestamp(timestamp)
                    .build();

            System.out.println("Current Thread: " + Thread.currentThread().getId());
            serviceStub.processQueryGRPC(identifier, new StreamObserver<BotGRPC.Acknowledgement>() {
                BotIdentity destination = botIdentity;
                ManagedChannel openChannel = channel;
                @Override
                public void onNext(BotGRPC.Acknowledgement value) {
                    counter--;
                    Logger.green("The response was positive! Still waiting for " + counter + " answers");
                }

                @Override
                public synchronized void onError(Throwable t) {
                    Logger.red("There was an error during the grpc");
                    if(t.getClass() == StatusRuntimeException.class) {
                        counter--;
                        BotUtilities.botRemovalFunction(destination, botThread);
                        channel.shutdown();
                        maintenanceAccess();
                    }
                }

                @Override
                public void onCompleted() {
                    channel.shutdown();
                    maintenanceAccess();
                }
            });
        }
    }

    /**
     * Method that simulates access to the mechanic
     */
    public void maintenanceAccess() {
        if(counter == 0){
            Logger.yellow("Starting the maintenance process");
            try {
                sleep(10000);
                Logger.green("The machine has gone back to normal");
            }catch(Exception e) {
                Logger.red("There was an error during wakeup procedure");
            }
            try {
                setOnMaintenance(false);
            } catch (AlreadyOnMaintenanceException e) {
                e.printStackTrace();
            }
            botServices.clearWaitingQueue();
            botThread.setTimestamp(-1);
            maintenanceCycle();
        }
    }

    public synchronized void setOnMaintenance(boolean onMaintenance)
            throws AlreadyOnMaintenanceException {
        if(this.onMaintenance && onMaintenance) {
            throw new AlreadyOnMaintenanceException();
        }
        this.onMaintenance = onMaintenance;
    }

    public void requestMaintenance() throws AlreadyOnMaintenanceException {
        setOnMaintenance(true);
        agrawalaProcedure();
    }
}
