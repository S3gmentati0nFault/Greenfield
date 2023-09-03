package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import cleaningBot.CommPair;
import cleaningBot.service.BotServices;
import com.google.errorprone.annotations.Var;
import extra.AtomicCounter.AtomicCounter;
import extra.Logger.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jdk.jfr.internal.tool.Main;
import services.grpc.BotGRPC;
import services.grpc.BotServicesGrpc;
import utilities.Variables;

import java.util.ArrayList;
import java.util.List;

import static utilities.Variables.DEBUGGING;

public class MutualExclusionThread extends Thread {

    private BotServices botServices;
    private AtomicCounter counter;
    private MaintenanceThread maintenanceThread;

    @Override
    public void run() {
        agrawalaProcedure();
    }

    public MutualExclusionThread(MaintenanceThread maintenanceThread, BotServices botServices) {
        this.maintenanceThread = maintenanceThread;
        this.botServices = botServices;
    }

    public MutualExclusionThread(MaintenanceThread maintenanceThread) {
        this.maintenanceThread = maintenanceThread;
    }

//    TODO
//    CONTROLLARE IL PROCESSO DI MUTUA ESCLUSIONE PERCHÈ SEMBRA CHE A VOLTE VENGA DUPLICATO O QUALCOSA DEL GENERE
    /**
     * Method that handles the mutual-exclusion by contacting via GRPC all the bots present
     * in the system at the moment of the malfunction
     */
    public synchronized void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");

        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
        List<BotIdentity> nonRespondingRobots = new ArrayList<>();

        counter = new AtomicCounter(fleetSnapshot.size());

        if(fleetSnapshot.isEmpty()){
            maintenanceAccess(null);
            return;
        }

        for (BotIdentity botIdentity : fleetSnapshot) {
            ManagedChannel channel;
            BotServicesGrpc.BotServicesStub serviceStub;

            CommPair communicationPair = BotThread.getInstance().getOpenComms().getValue(botIdentity);
            if(communicationPair == null) {
                channel = ManagedChannelBuilder
                        .forTarget(botIdentity.getIp() + ":" + botIdentity.getPort())
                        .usePlaintext()
                        .build();

                serviceStub = BotServicesGrpc.newStub(channel);
                BotThread.getInstance().newCommunicationChannel(botIdentity, channel, serviceStub);
            }
            else {
                channel = communicationPair.getManagedChannel();
                serviceStub = communicationPair.getCommunicationStub();
            }

            long timestamp = System.currentTimeMillis();
            BotThread.getInstance().setTimestamp(timestamp);
            BotGRPC.Identifier identifier = BotGRPC.Identifier
                    .newBuilder()
                    .setId(botIdentity.getId())
                    .setTimestamp(timestamp)
                    .build();

            serviceStub.maintenanceRequestGRPC(identifier, new StreamObserver<BotGRPC.Acknowledgement>() {
                @Override
                public synchronized void onNext(BotGRPC.Acknowledgement value) {
                    if(DEBUGGING) {
                        System.out.println("FUORI -> " + Thread.currentThread().getId());
                    }
                    synchronized (this) {
                        if(DEBUGGING) {
                            System.out.println("DENTRO -> " + Thread.currentThread().getId());
                        }
                        counter.decrement();
                        Logger.green("The response was positive! Still waiting for " + counter.getCounter() + " answers");
                    }
                }

                @Override
                public synchronized void onError(Throwable t) {
                    Logger.red("Robot " + botIdentity.getId() + " did not reply to my maintenanceRequest call");
                    Logger.green("Defaulting to positive reply, still waiting for " + counter.getCounter() + " answers");
                    counter.decrement();
                    nonRespondingRobots.add(botIdentity);
                    maintenanceAccess(nonRespondingRobots);
                }

                @Override
                public synchronized void onCompleted() {
                    maintenanceAccess(nonRespondingRobots);
                }
            });
        }
    }

//    TODO
//    >> FLAVOUR :: EFFICENZA-ARANCIONE <<
//    TRASFORMARE IL METODO DI BOTREMOVALFUNCTION IN UN THREAD
    /**
     * Method that simulates access to the mechanic
     */
    public synchronized void maintenanceAccess(List<BotIdentity> nonRespondingRobots) {
        if(counter.getCounter() == 0){
            counter.add(10);
            Logger.yellow("Starting the maintenance process");

            if(nonRespondingRobots != null) {
                if(!nonRespondingRobots.isEmpty()) {
                    BotUtilities.botRemovalFunction(nonRespondingRobots, false);
                }
            }

            maintenanceThread.setDoingMaintenance(true);
            try {
                sleep(10000);
                Logger.green("The machine has gone back to normal");
            }catch(Exception e) {
                Logger.red(Variables.WAKEUP_ERROR, e.getCause());
            }

            maintenanceThread.setDoingMaintenance(false);
            MeasurementGatheringThread measurementGatheringThread = BotThread.getInstance().getMeasurementGatheringThread();
            PollutionSensorThread sensor = BotThread.getInstance().getPollutionSensorThread();
            synchronized (measurementGatheringThread) {
                measurementGatheringThread.notifyAll();
            }
            synchronized (sensor) {
                sensor.notifyAll();
            }

            BotThread.getInstance().getBotServices().clearWaitingQueue();
            BotThread.getInstance().setTimestamp(-1);
            BotThread.getInstance().getInputThread().wakeupHelper();
            maintenanceThread.wakeupMaintenanceThread();
        }
    }
}
