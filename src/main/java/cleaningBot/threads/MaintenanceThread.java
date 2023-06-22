package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.service.BotServices;
import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Variables;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceThread extends Thread {
    private boolean onMaintenance;
    private BotThread botThread;
    private BotServices botServices;

    public MaintenanceThread(BotThread botThread, BotServices botServices) {
        onMaintenance = false;

        this.botThread = botThread;
        this.botServices = botServices;
    }

    @Override
    public void run() {
        maintenanceCycle();
    }

    public void maintenanceCycle() {
        while(!onMaintenance) {
            System.out.println("Rolling The Dice");
            try{
                sleep(5000);
            } catch (Exception e) {
                Logger.red("There was a problem while waking up from sleep");
            }
            if(CustomRandom.getInstance().probability(3)){
                Logger.yellow("The system has broken down and needs to be repaired");
                onMaintenance = true;
                agrawalaProcedure();
            }
        }
    }

    public synchronized void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");
        List<BotIdentity> fleetSnapshot = botThread.getOtherBots();
        List<ManagedChannel> channels = new ArrayList<ManagedChannel>();

        fleetSnapshot.remove(
                new BotIdentity(botThread.getIdentity().getId(),
                        botThread.getIdentity().getPort(),
                        botThread.getIdentity().getIp()
                )
        );

        if(fleetSnapshot.size() == 0){
            maintenanceAccess();
            return;
        }

        StreamObserver<BotGRPC.Acknowledgement> streamObserver =
                new StreamObserver<BotGRPC.Acknowledgement>() {
            int counter = 0;
            int total = fleetSnapshot.size();
                @Override
                public void onNext(BotGRPC.Acknowledgement value) {
                    counter++;
                    Logger.green("The response was positive! We ar at " + counter + " over " + total);
                }

                @Override
                public void onError(Throwable t) {
                    Logger.red("There was an error during the grpc");
                }

                @Override
                public void onCompleted() {
                    if(counter == total){
                        maintenanceAccess();
                        Logger.yellow("Shutting down communication channels");
                        for (ManagedChannel channel : channels) {
                            channel.shutdown();
                        }
                        channels.clear();
                    }
                }
        };

        for (BotIdentity botIdentity : fleetSnapshot) {
            Logger.cyan("Contacting the other bots");

            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                    .usePlaintext()
                    .build();

            channels.add(channel);

            BotServicesGrpc.BotServicesStub serviceStub = BotServicesGrpc.newStub(channel);

            long timestamp = System.currentTimeMillis();
            if(Variables.MODE.equals("DEBUG")) {
                System.out.println(timestamp);
            }
            botThread.setTimestamp(timestamp);
            BotGRPC.Identifier identifier = BotGRPC.Identifier
                    .newBuilder()
                    .setId(botIdentity.getId())
                    .setTimestamp(timestamp)
                    .build();

            serviceStub.processQueryGRPC(identifier, streamObserver);
        }
    }

    public void maintenanceAccess() {
        Logger.yellow("Starting the maintenance process");
        try {
            sleep(10000);
            Logger.green("The machine has gone back to normal");
        }catch(Exception e) {
            Logger.red("There was an error during wakeup procedure");
        }
        onMaintenance = false;
        botServices.clearWaitingQueue();
        botThread.setTimestamp(-1);
        maintenanceCycle();
    }
}
