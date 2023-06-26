package cleaningBot.service;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import cleaningBot.threads.BotThread;
import cleaningBot.threads.MaintenanceThread;
import extra.Logger.Logger;
import io.grpc.StatusRuntimeException;
import services.grpc.BotGRPC;
import io.grpc.stub.StreamObserver;

public class QueryResponseObserver {
    private StreamObserver<BotGRPC.Acknowledgement> streamObserver;
    private MaintenanceThread maintenanceThread;

    public QueryResponseObserver(MaintenanceThread maintenanceThread) {
        this.maintenanceThread = maintenanceThread;
    }

    public StreamObserver<BotGRPC.Acknowledgement> createStreamObserver(BotIdentity destination, BotThread originator) {
        streamObserver = new StreamObserver<BotGRPC.Acknowledgement>() {
            BotIdentity deadRobot = destination;
            BotThread bot = originator;
            MaintenanceThread thread = maintenanceThread;

            @Override
            public void onNext(BotGRPC.Acknowledgement value) {}

            @Override
            public void onError(Throwable t) {
                Logger.red("There was an error during the grpc");
                if(t.getClass() == StatusRuntimeException.class) {
                    BotUtilities.botRemovalFunction(deadRobot, bot);
                }
            }

            @Override
            public void onCompleted() {
                thread.maintenanceAccess();
            }
        };

        return streamObserver;
    }
}
