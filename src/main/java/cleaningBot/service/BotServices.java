package cleaningBot.service;

import beans.BotIdentity;
import cleaningBot.threads.BotThread;
import extra.Logger.Logger;
import io.grpc.stub.StreamObserver;
import services.grpc.*;
import services.grpc.BotServicesGrpc.*;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * This class implements the GRPC methods defined inside the proto file.
 */
public class BotServices extends BotServicesImplBase {

    private Queue<WaitingThread> waitingInstances;
    private BotThread botThread;
    private ServiceComparator comparator;

    /**
     * Public constructor. Public constructor that builds the comparator necessary to
     * handle the data structure used to keep track of the threads that still need
     * answering.
     * @param botThread the identity of the parent Thread, used for timestamp comparisons
     *                  when remote requests arrive.
     */
    public BotServices(BotThread botThread) {
        this.botThread = botThread;
        comparator = new ServiceComparator();

        waitingInstances = new PriorityQueue<>(comparator);
    }

    /**
     * Method that processes a query to access the Mutual-exclusion zone (the mechanic).
     * Method that processes a query to access the mechanic, it is synchronized because it
     * writes on the data structure associated to the Thread.
     * @param request The request contains both the timestamp and the identifier of the
     *                asking thread.
     * @param responseObserver The callback function.
     */
    public void processQueryGRPC(BotGRPC.Identifier request,
                                 StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("processQueryGRPC");
        System.out.println("Current Thread: " + Thread.currentThread().getId());
        if(request.getTimestamp() > botThread.getTimestamp() &&
                botThread.getTimestamp() != -1){

            Logger.yellow("The message has a timestamp greater than mine");
            waitingInstances.add(new WaitingThread(this, request.getTimestamp()));
            try{
                wait();
            }catch(Exception e){
                Logger.red("There was an error during the wakeup process");
            }
            waitingInstances.remove(new WaitingThread(this, request.getTimestamp()));
        }
        else{
            Logger.yellow("The message has a lower timestamp than mine");
        }
        responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Method that processes the addition of a new robot to the network.
     * @param request The request contains all the useful information about the new robot.
     * @param responseObserver The callback function.
     */
    public void joinAdvertiseGRPC(BotGRPC.BotNetworkingInformations request,
        StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("joinAdvertiseGRPC");

        try{
            if(botThread.getOtherBots().contains(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost()))) {
                responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
            }
            else{
                botThread.getOtherBots().add(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost())
                );
                responseObserver.onNext(BotGRPC.Acknowledgement
                        .newBuilder()
                        .setAck(true)
                        .build());
            }
        }catch(Exception e){
            Logger.red(":'(");
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    public void crashAdvertiseGRPC(BotGRPC.BotNetworkingInformations request,
        StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("crashAdvertiseGRPC");

        try {
            botThread.removeBot(
                    new BotIdentity(request.getId(), request.getPort(), request.getHost())
            );
        } catch(Exception e) {
            responseObserver.onError(e);
        }

        responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Method that allows the local thread to clean up the waiting queue once access to the
     * Mutual-exclusion area is done.
     */
    public synchronized void clearWaitingQueue() {
        if(waitingInstances.size() != 0) {
            waitingInstances.forEach(
                service -> {
                    Logger.yellow("Waking service " + service.getTimestamp() + " up");
                    service.getBotServices().notify();
                }
            );
        }
        waitingInstances.clear();
    }

    /**
     * Getter for the botThread variable.
     */
    public BotThread getBotThread() {
        return botThread;
    }
}
