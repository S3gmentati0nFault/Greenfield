package cleaningBot;

import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import cleaningBot.threads.BotEntry;
import extra.Logger.Logger;
import io.grpc.stub.StreamObserver;
import services.grpc.*;
import services.grpc.BotServicesGrpc.*;

import java.util.PriorityQueue;

/**
 * This class implements the GRPC methods defined inside the proto file.
 */
public class BotServices extends BotServicesImplBase {
    private PriorityQueue<BotEntry> contentionQueue;
    private BotThread botThread;

    public BotServices(BotThread botThread) {
        this.botThread = botThread;

        contentionQueue = new PriorityQueue<>();
    }

    public PriorityQueue<BotEntry> getContentionQueue() {
        return contentionQueue;
    }

    public void processQueryGRPC(BotGRPC.Identifier request,
                                 StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.cyan("processQueryGRPC");
    }

    public void joinAdvertiseGRPC(BotGRPC.BotNetworkingInformations request,
        StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.cyan("joinAdvertiseGRPC");

        try{
            botThread.getOtherBots().add(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost())
            );
            responseObserver.onNext(BotGRPC.Acknowledgement
                    .newBuilder()
                    .setAck(true)
                    .build());
        }catch(Exception e){
            Logger.red(":'(");
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    public void crashAdvertiseGRPC(BotGRPC.BotNetworkingInformations request,
        StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.cyan("crashAdvertiseGRPC");

        try{
            botThread.getOtherBots().remove(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost())
            );
        }catch(Exception e){
            responseObserver.onError(e);
        }
        botThread.getOtherBots().forEach(botIdentity -> {System.out.println(botIdentity);});
        responseObserver.onCompleted();
    }
}
