package cleaningBot.service;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import cleaningBot.Position;
import cleaningBot.threads.BotThread;
import extra.Logger.Logger;
import io.grpc.stub.StreamObserver;
import services.grpc.*;
import services.grpc.BotServicesGrpc.*;
import utilities.Variables;

import java.util.*;

import static java.lang.Thread.sleep;
import static utilities.Variables.*;

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
     *
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
     *
     * @param request          The request contains both the timestamp and the identifier of the
     *                         asking thread.
     * @param responseObserver The callback function.
     */
    public synchronized void maintenanceRequestGRPC(BotGRPC.Identifier request,
                                                    StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("processQueryGRPC");
        if (DEBUGGING) {
            System.out.println("Current Thread: " + Thread.currentThread().getId());
        }
        if (request.getTimestamp() > botThread.getTimestamp() &&
                botThread.getTimestamp() != -1) {
            Logger.yellow("The message has a timestamp greater than mine");
            waitingInstances.add(new WaitingThread(this, request.getTimestamp()));
            try {
                Logger.whiteDebuggingPrint(this.getClass() + ".maintenanceRequest IS WAITING", AGRAWALA_DEBUGGING);
                wait(1000000);
            } catch (Exception e) {
                Logger.red(WAKEUP_ERROR);
                e.printStackTrace();
            }
            Logger.whiteDebuggingPrint(this.getClass() + ".maintenanceRequest IS NOT WAITING", AGRAWALA_DEBUGGING);
            waitingInstances.remove(new WaitingThread(this, request.getTimestamp()));
        } else {
            Logger.yellow("The message has a lower timestamp than mine");
        }
        responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Method that processes the addition of a new robot to the network.
     *
     * @param request          The request contains all the useful information about the new robot.
     * @param responseObserver The callback function.
     */
    public void joinRequestGRPC(BotGRPC.BotInformation request,
                                StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("joinAdvertiseGRPC");

        BotIdentity newBot = new BotIdentity(request.getId(), request.getPort(), request.getHost(),
                new Position(request.getPosition().getX(), request.getPosition().getY()));

        List<BotIdentity> currentFleet = botThread.getOtherBots().getArrayList();
        try {
            if (currentFleet.contains(newBot)) {
                responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
            } else {
                currentFleet.add(newBot);
                responseObserver.onNext(BotGRPC.Acknowledgement
                        .newBuilder()
                        .setAck(true)
                        .build());
            }
        } catch (Exception e) {
            Logger.red(":'(");
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    public void crashNotificationGRPC(BotGRPC.DeadRobotList request,
                                      StreamObserver<BotGRPC.IntegerValue> responseObserver) {
        Logger.purple("crashAdvertiseGRPC");

        List<BotGRPC.BotInformation> informations = request.getDeadRobotsList();
        List<BotIdentity> deadRobots = new ArrayList<>();
        for (BotGRPC.BotInformation information : informations) {
            BotIdentity deadBot = new BotIdentity(information.getId(), information.getPort(), information.getHost(),
                    new Position(information.getPosition().getX(), information.getPosition().getY()));
            deadRobots.add(deadBot);
        }

        if (botThread.removeBot(deadRobots)) {
            responseObserver.onNext(BotGRPC.IntegerValue.newBuilder().setValue(1).build());
            responseObserver.onCompleted();
        }
    }

    public void moveRequestGRPC(BotGRPC.IntegerValue request,
                                StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("moveRequestGRPC");

        if (botThread.getDistrict() == request.getValue()) {
            Logger.red("I'm already present in that district");
            responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
            responseObserver.onCompleted();
        } else {
            synchronized (this) {
                botThread.changeMyPosition(request.getValue());
                try {
                    Logger.whiteDebuggingPrint(this.getClass() + ".moveRequest IS WAITING", MOVE_REQUEST_DEBUGGING);
                    wait();
                } catch (InterruptedException e) {
                    Logger.red(WAKEUP_ERROR, e);
                }
                Logger.whiteDebuggingPrint(this.getClass() + ".moveRequest IS NOT WAITING", MOVE_REQUEST_DEBUGGING);

                responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
                responseObserver.onCompleted();
            }
        }
    }

    public synchronized void positionModificationRequestGRPC(BotGRPC.BotInformation request,
                                                             StreamObserver<BotGRPC.Acknowledgement> responseObserver) {
        Logger.purple("positionModificationRequestGRPC");

        if(MOVE_REQUEST_DEBUGGING) {
            Logger.blue("PUTTING MYSELF TO SLEEP");
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        botThread.updatePosition(new BotIdentity(request.getId(), request.getPort(), "localhost",
                new Position(request.getPosition().getX(), request.getPosition().getY())));
        responseObserver.onNext(BotGRPC.Acknowledgement.newBuilder().setAck(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Method that allows the local thread to clean up the waiting queue once access to the
     * Mutual-exclusion area is done.
     */
    public synchronized void clearWaitingQueue() {
        if (!waitingInstances.isEmpty()) {
            waitingInstances.forEach(
                    service -> {
                        if(QUEUE_DEBUGGING) {
                            Logger.blue("SLEEPING");
                            try {
                                sleep(10000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        Logger.yellow("Waking service " + service.getTimestamp() + " up");
                        service.getBotServices().notify();
                    }
            );
        }
        waitingInstances.clear();
    }

}