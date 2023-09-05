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

import static utilities.Variables.*;

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

    /**
     * Method that handles the mutual-exclusion by contacting via GRPC all the bots present
     * in the system at the moment of the malfunction
     */
    public synchronized void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");

        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
        List<BotIdentity> nonRespondingRobots = new ArrayList<>();

        counter = new AtomicCounter(fleetSnapshot.size());

        if (fleetSnapshot.isEmpty()) {
            maintenanceAccess(null);
            return;
        }

        for (BotIdentity botIdentity : fleetSnapshot) {
            ManagedChannel channel;
            BotServicesGrpc.BotServicesStub serviceStub;

            CommPair communicationPair = BotUtilities.retrieveCommunicationPair(botIdentity);

            long timestamp = System.currentTimeMillis();
            BotThread.getInstance().setTimestamp(timestamp);
            BotGRPC.Identifier identifier = BotGRPC.Identifier
                    .newBuilder()
                    .setId(botIdentity.getId())
                    .setTimestamp(timestamp)
                    .build();

//            TODO
//            >> FLAVOUR :: DEBUGGING-GIALLO <<
//            CONTROLLARE UN COMPORTAMENTO STRANO PER CUI, QUALCHE VOLTA, LA STAMPA DEL CONTATORE VIENE RIPETUTA, COME SE
//            NON CI FOSSE SINCRONIZZAZIONE O COME SE IL THREAD CORRENTE RILASCIASSE SUBITO IL MONITOR.
//            ESEMPIO: 1, 1, 0 ANZICHÈ 2, 1, 0
            communicationPair.getCommunicationStub().maintenanceRequestGRPC(identifier,
                    new StreamObserver<BotGRPC.Acknowledgement>() {
                        @Override
                        public synchronized void onNext(BotGRPC.Acknowledgement value) {
                            Logger.whiteDebuggingPrint("onNext", MUTUAL_EXCLUSION_DEBUGGING);
                            counter.decrement();
//                            if(MUTUAL_EXCLUSION_DEBUGGING) {
//                                try {
//                                    sleep(5000);
//                                } catch (InterruptedException e) {
//                                    Logger.red(WAKEUP_ERROR, e);
//                                }
//                            }
                            Logger.green("The response was positive! Still waiting for " + counter.getCounter() + " answers");
                        }

                        @Override
                        public synchronized void onError(Throwable t) {
                            Logger.red("Robot " + botIdentity.getId() + " did not reply to my maintenanceRequest call");
                            counter.decrement();
                            Logger.green("Defaulting to positive reply, still waiting for " + counter.getCounter() + " answers");
                            nonRespondingRobots.add(botIdentity);
                            maintenanceAccess(nonRespondingRobots);
                        }

                        @Override
                        public synchronized void onCompleted() {
                            Logger.whiteDebuggingPrint("onCompleted", MUTUAL_EXCLUSION_DEBUGGING);
                            maintenanceAccess(nonRespondingRobots);
                        }
                    });
        }
    }

    /**
     * Method that simulates access to the mechanic
     */
    public synchronized void maintenanceAccess(List<BotIdentity> nonRespondingRobots) {
        if (counter.getCounter() == 0) {
            counter.add(10);
            Logger.cyan("Starting the maintenance process");

            if (nonRespondingRobots != null) {
                if (!nonRespondingRobots.isEmpty()) {
                    Logger.yellow("Starting the eliminator thread to delete " + nonRespondingRobots.size());
                    EliminatorThread eliminatorThread = new EliminatorThread(nonRespondingRobots, false);
                    eliminatorThread.start();
                }
            }

            maintenanceThread.setDoingMaintenance(true);
            try {
                sleep(10000);
                Logger.green("The machine has gone back to normal");
            } catch (Exception e) {
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

            QuitHelperThread quitHelperThread = BotThread.getInstance().getInputThread().getQuitHelperThread();
            if (quitHelperThread != null) {
                synchronized (quitHelperThread) {
                    quitHelperThread.notify();
                }
            }

            BotThread.getInstance().setTimestamp(-1);
            maintenanceThread.wakeupMaintenanceThread();
        }
    }
}
