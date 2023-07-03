package cleaningBot.threads;

import cleaningBot.BotUtilities;
import extra.Logger.Logger;

public class QuitHelperThread extends Thread {
    @Override
    public synchronized void run() {
        Logger.yellow("Waiting for maintenance to finish");
        if(BotThread.getInstance().getMaintenanceThread().getOnMaintenance()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red("There was an error while trying to wake up");
            }
        }
        Logger.yellow("Removing the robot from the city");
        BotUtilities.botRemovalFunction(BotThread.getInstance().getIdentity());
    }

    public synchronized void wakeup() {
        notify();
    }
}
