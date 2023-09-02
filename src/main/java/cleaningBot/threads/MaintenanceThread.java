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
    private boolean onMaintenance;
    private final int PROBABILITY;
    private final long TIMEOUT;


    /**
     * Generic public constructor
     */
    public MaintenanceThread(BotServices botServices) {
        this.botServices = botServices;
        onMaintenance = false;
        if(DEBUGGING) {
            PROBABILITY = DEBUGGING_MAINTENANCE_PROBABILITY;
            TIMEOUT = DEBUGGING_TIMEOUT;
        }
        else {
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
        if (onMaintenance) {
            throw new AlreadyOnMaintenanceException();
        }

        setOnMaintenance(true);
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

        setOnMaintenance(false);
        PollutionSensorThread sensor = BotThread.getInstance().getPollutionSensorThread();
        MeasurementGatheringThread measurementGatheringThread = BotThread.getInstance().getMeasurementGatheringThread();
        synchronized (sensor) {
            sensor.notify();
        }
        synchronized (measurementGatheringThread) {
            measurementGatheringThread.notify();
        }
    }

    public synchronized void wakeupMaintenanceThread() {
        notifyAll();
    }

    public synchronized boolean getOnMaintenance() {
        return onMaintenance;
    }

    public synchronized void setOnMaintenance(boolean onMaintenance)
            throws AlreadyOnMaintenanceException {
        if (this.onMaintenance && onMaintenance) {
            throw new AlreadyOnMaintenanceException();
        }
        this.onMaintenance = onMaintenance;
    }
}
