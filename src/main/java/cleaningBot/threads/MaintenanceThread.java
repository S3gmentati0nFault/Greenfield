package cleaningBot.threads;

import cleaningBot.service.BotServices;
import exceptions.AlreadyOnMaintenanceException;
import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Variables;

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
        while(true) {
            if(CustomRandom.getInstance().probability(3)){
                try{
                    Logger.blue("The robot should undergo maintenance");
                    if(Variables.AGRAWALA){
                        Logger.blue("TRY USING THE FIX COMMAND");
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
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
            if(Variables.DEBUG) {
                System.out.println("MAINTENANCE PRECHECK");
            }
            setOnMaintenance(true);
            MutualExclusionThread mutualExclusionThread =
                new MutualExclusionThread(this, botServices);
            mutualExclusionThread.start();
            if(Variables.DEBUG) {
                System.out.println("WAITING");
            }
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(Variables.DEBUG) {
                System.out.println("NOT WAITING");
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
