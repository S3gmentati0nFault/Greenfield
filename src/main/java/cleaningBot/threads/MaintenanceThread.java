package cleaningBot.threads;

import cleaningBot.service.BotServices;
import exceptions.AlreadyOnMaintenanceException;
import extra.Logger.Logger;
import utilities.Variables;

import java.util.Random;

import static utilities.Variables.*;

/**
 * Maintenance class that simulates the error rate of the bots and handles the
 * Mutual-exclusion
 */
public class MaintenanceThread extends Thread {
    private BotServices botServices;
    private boolean inQueue;
    private boolean doingMaintenance;
    private final int PROBABILITY;
    private final long TIMEOUT;


    /**
     * Generic public constructor
     */
    public MaintenanceThread(BotServices botServices) {
        this.botServices = botServices;
        inQueue = false;
        if (HIGH_COLLISION_MODE) {
            PROBABILITY = DEBUGGING_MAINTENANCE_PROBABILITY;
            TIMEOUT = DEBUGGING_TIMEOUT;
        } else {
            PROBABILITY = MAINTENANCE_PROBABILITY;
            TIMEOUT = MAINTENANCE_TIMEOUT;
        }
    }

    /**
     * Override of the run method that starts the maintenance cycle, which simulates the
     * malfunction of the robots
     */
    @Override
    public void run() {
        maintenanceCycle();
    }

    /**
     * Method that simulates the malfunction of the robots
     */
    public void maintenanceCycle() {
        Random random = new Random();
        while (true) {
            if (random.nextInt(9) < PROBABILITY) {
                try {
                    Logger.blue("The robot should undergo maintenance");
                    doMaintenance();
                } catch (AlreadyOnMaintenanceException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(TIMEOUT);
            } catch (Exception e) {
                Logger.red(Variables.WAKEUP_ERROR, e.getCause());
            }
        }
    }

    public void doMaintenance()
            throws AlreadyOnMaintenanceException {
        synchronized (this) {
            try {
                setInQueue(true);
            } catch (AlreadyOnMaintenanceException e) {
                Logger.red("The robot is already in queue for maintenance, this request will be skipped");
                return;
            }
        }

        MutualExclusionThread mutualExclusionThread =
                new MutualExclusionThread(this);
        mutualExclusionThread.start();

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red(Variables.WAKEUP_ERROR, e.getCause());
            }
        }

        setInQueue(false);
    }

    public synchronized void wakeupMaintenanceThread() {
        notifyAll();
    }

    public synchronized boolean isInQueue() {
        return inQueue;
    }

    public synchronized void setInQueue(boolean inQueue)
            throws AlreadyOnMaintenanceException {
        if (this.inQueue && inQueue) {
            throw new AlreadyOnMaintenanceException();
        }
        this.inQueue = inQueue;
    }

    public boolean isDoingMaintenance() {
        return doingMaintenance;
    }

    public void setDoingMaintenance(boolean doingMaintenance) {
        this.doingMaintenance = doingMaintenance;
    }
}
