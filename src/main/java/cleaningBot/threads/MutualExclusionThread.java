package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import cleaningBot.service.BotServices;
import extra.Logger.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.util.List;

public class MutualExclusionThread extends Thread {

    private BotServices botServices;
    private int counter;
    private MaintenanceThread maintenanceThread;

    @Override
    public void run() {
        agrawalaProcedure();
    }

    public MutualExclusionThread(MaintenanceThread maintenanceThread, BotServices botServices) {
        this.maintenanceThread = maintenanceThread;
        this.botServices = botServices;
    }

    /**
     * Method that handles the mutual-exclusion by contacting via GRPC all the bots present
     * in the system at the moment of the malfunction
     */
    public synchronized void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");
        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots();

        if(fleetSnapshot.size() == 0){
            maintenanceAccess();
            return;
        }

        counter = fleetSnapshot.size();

        for (BotIdentity botIdentity : fleetSnapshot) {

            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();

            BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

            long timestamp = System.currentTimeMillis();
            BotThread.getInstance().setTimestamp(timestamp);
            BotGRPC.Identifier identifier = BotGRPC.Identifier
                    .newBuilder()
                    .setId(botIdentity.getId())
                    .setTimestamp(timestamp)
                    .build();

            serviceStub.processQueryGRPC(identifier, new StreamObserver<BotGRPC.Acknowledgement>() {
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
                        BotUtilities.botRemovalFunction(botIdentity, false);
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
            botServices.clearWaitingQueue();
            BotThread.getInstance().setTimestamp(-1);
            BotThread.getInstance().getInputThread().wakeupHelper();
            maintenanceThread.wakeupMaintenanceThread();
        }
    }
}
