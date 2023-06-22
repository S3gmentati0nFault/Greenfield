package cleaningBot;

import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import cleaningBot.threads.BotEntry;
import extra.Logger.Logger;
import extra.Variables;
import io.grpc.stub.StreamObserver;
import services.grpc.*;
import services.grpc.BotServicesGrpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class implements the GRPC methods defined inside the proto file.
 */
public class BotServices extends BotServicesImplBase {

    private List<BotServices> waitingInstances;
    private BotThread botThread;

    public BotServices(BotThread botThread) {
        this.botThread = botThread;

        waitingInstances = new ArrayList<BotServices>();
    }

    public synchronized void processQueryGRPC(BotGRPC.Identifier request,
                                 StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("processQueryGRPC");

        if(request.getTimestamp() > botThread.getTimestamp() &&
                botThread.getTimestamp() != -1){

            Logger.yellow("The message has a timestamp greater than mine");
            waitingInstances.add(this);
            if(Variables.MODE.equals("DEBUG")){
                System.out.println("Waiting");
            }
            try{
                wait();
            }catch(Exception e){
                Logger.red("There was an error during the wakeup process");
            }
            if(Variables.MODE.equals("DEBUG")) {
                System.out.println("Not waiting anymore");
            }
        }
        else{
            Logger.yellow("The message has a lower timestamp than mine");
        }
        responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    public void joinAdvertiseGRPC(BotGRPC.BotNetworkingInformations request,
        StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("joinAdvertiseGRPC");

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
        Logger.purple("crashAdvertiseGRPC");

        try{
            botThread.getOtherBots().remove(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost())
            );
        }catch(Exception e){
            responseObserver.onError(e);
        }
        if(Variables.MODE.equals("DEBUG")) {
            botThread.getOtherBots().forEach(botIdentity -> {
                System.out.println(botIdentity);
            });
        }
        responseObserver.onCompleted();
    }

    public synchronized void clearWaitingQueue() {
        if(waitingInstances.size() != 0){
            waitingInstances.forEach(
                service -> {
                    if(Variables.MODE.equals("DEBUG")) {
                        System.out.println(service.getBotThread().getIdentity());
                    }
                    Logger.yellow("Waking services up");
                    service.notify();
                }
            );
        }
    }

    public List<BotServices> getWaitingInstances() {
        return waitingInstances;
    }

    public BotThread getBotThread() {
        return botThread;
    }


}
