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

import java.util.List;

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

        counter = new AtomicCounter(fleetSnapshot.size());

        if(fleetSnapshot.isEmpty()){
            maintenanceAccess();
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
                public void onNext(BotGRPC.Acknowledgement value) {
                    System.out.println("FUORI -> " + Thread.currentThread().getId());
                    synchronized (this) {
                        System.out.println("DENTRO -> " + Thread.currentThread().getId());
                        counter.decrement();
                        Logger.green("The response was positive! Still waiting for " + counter.getCounter() + " answers");
                    }
                }

//                TODO
//                QUANDO SI PRESENTA L'ERRORE, ANZICHÈ CHIAMARE SUBITO LA FUNZIONE DI RIMOZIONE AGGIUNGERE IL ROBOT DA
//                ELIMINARE A UNA STRUTTURA LOCALE, SE ARRIVANO DEI MESSAGGI PER LA SUA ELIMINAZIONE SARÀ ELIMINATO DALLA
//                STRUTTURA OPPURE TUTTE LE RICHIESTE VERRANNO IGNORATE SUCCESSIVAMENTE
                @Override
                public synchronized void onError(Throwable t) {
                    Logger.red("There was an error during the grpc");
                    if(t.getClass() == StatusRuntimeException.class) {
                        counter.decrement();
                        BotUtilities.botRemovalFunction(botIdentity, false);
                        maintenanceAccess();
                    }
                }

                @Override
                public void onCompleted() {
                    maintenanceAccess();
                }
            });
        }
    }

    /**
     * Method that simulates access to the mechanic
     */
    public void maintenanceAccess() {
        if(counter.getCounter() == 0){
            Logger.yellow("Starting the maintenance process");
            try {
                sleep(10000);
                Logger.green("The machine has gone back to normal");
            }catch(Exception e) {
                Logger.red(Variables.WAKEUP_ERROR, e.getCause());
            }
//            botServices.clearWaitingQueue();
            BotThread.getInstance().getBotServices().clearWaitingQueue();
            BotThread.getInstance().setTimestamp(-1);
            BotThread.getInstance().getInputThread().wakeupHelper();
            maintenanceThread.wakeupMaintenanceThread();
        }
    }
}
