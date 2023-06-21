package cleaningBot.threads;

import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;

public class MaintenanceThread extends Thread {
    private boolean onMaintenance;

    public MaintenanceThread() {
        onMaintenance = false;
    }

    @Override
    public void run() {
        while(!onMaintenance) {
            System.out.println("Rolling The Dice");
            try{
                sleep(5000);
            } catch (Exception e) {
                Logger.red("There was a problem while waking up from sleep");
            }
            if(CustomRandom.getInstance().probability(3)){
                Logger.yellow("The system has broken down and needs to be repaired");
                onMaintenance = true;
                agrawalaProcedure();
            }
        }
    }

    public void agrawalaProcedure() {
        Logger.cyan("Starting the Agrawala procedure");
    }
}
