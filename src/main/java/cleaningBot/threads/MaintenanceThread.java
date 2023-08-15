package cleaningBot.threads;

import cleaningBot.service.BotServices;
import exceptions.AlreadyOnMaintenanceException;
import extra.Logger.Logger;

import java.util.Random;

/**
 * Maintenance class that simulates the error rate of the bots and handles the
 * Mutual-exclusion
 */
public class MaintenanceThread extends Thread {
    private BotServices botServices;
    private boolean onMaintenance;

    /**
     * Generic public constructor
     */
    public MaintenanceThread(BotServices botServices) {
        this.botServices = botServices;
        onMaintenance = false;
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
        while(true) {
            if(random.nextInt(9) < 3){
                try{
                    Logger.blue("The robot should undergo maintenance");
                    doMaintenance();
                } catch (AlreadyOnMaintenanceException e) {

                    e.printStackTrace();
                }
            }
            try{
                sleep(5000);
            } catch (Exception e) {
                Logger.red("There was a problem while waking up from sleep");
            }
        }
    }

    public synchronized void doMaintenance()
            throws AlreadyOnMaintenanceException {
        if(!onMaintenance){
            setOnMaintenance(true);
            MutualExclusionThread mutualExclusionThread =
                new MutualExclusionThread(this, botServices);
            mutualExclusionThread.start();
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setOnMaintenance(false);
        }
        else{
            throw new AlreadyOnMaintenanceException();
        }
    }

    public synchronized void wakeupMaintenanceThread() {
        notifyAll();
    }

    public boolean getOnMaintenance() {
        return onMaintenance;
    }

    public synchronized void setOnMaintenance(boolean onMaintenance)
            throws AlreadyOnMaintenanceException {
        if(this.onMaintenance && onMaintenance) {
            throw new AlreadyOnMaintenanceException();
        }
        this.onMaintenance = onMaintenance;
    }
}
