package cleaningBot.service;

import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import extra.Logger.Logger;
import extra.Variables;
import io.grpc.stub.StreamObserver;
import services.grpc.*;
import services.grpc.BotServicesGrpc.*;

import java.util.PriorityQueue;
import java.util.Queue;

import static extra.Variables.TIMEOUT_MILLIS;
import static java.lang.Thread.sleep;

/**
 * This class implements the GRPC methods defined inside the proto file.
 */
public class BotServices extends BotServicesImplBase {

    private Queue<ServiceEntry> waitingInstances;
    private BotThread botThread;
    private ServiceComparator comparator;

    public BotServices(BotThread botThread) {
        this.botThread = botThread;
        comparator = new ServiceComparator();

        waitingInstances = new PriorityQueue<>(comparator);
    }

    public synchronized void processQueryGRPC(BotGRPC.Identifier request,
                                 StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("processQueryGRPC");

        if(request.getTimestamp() > botThread.getTimestamp() &&
                botThread.getTimestamp() != -1){

            Logger.yellow("The message has a timestamp greater than mine");
            waitingInstances.add(new ServiceEntry(this, request.getTimestamp()));
            if(Variables.MODE.equals("DEBUG")){
                System.out.println("Waiting");
            }
            try{
                wait(TIMEOUT_MILLIS);
            }catch(Exception e){
                Logger.red("There was an error during the wakeup process");
            }
            if(Variables.MODE.equals("DEBUG")) {
                System.out.println("Not waiting anymore");
            }
            waitingInstances.remove(new ServiceEntry(this, request.getTimestamp()));
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
        try {
            sleep(50000);
        }catch(Exception e) {
            Logger.red("There was an error while trying to wake up");
        }

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
                        System.out.println(service.getBotServices().getBotThread().getIdentity());
                    }
                    Logger.yellow("Waking service " + service.getTimestamp() + " up");
                    service.getBotServices().notify();
                }
            );
        }
    }

    public Queue<ServiceEntry> getWaitingInstances() {
        return waitingInstances;
    }

    public BotThread getBotThread() {
        return botThread;
    }


}
