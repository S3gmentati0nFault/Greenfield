package cleaningBot.threads;

import cleaningBot.BotUtilities;
import extra.Logger.Logger;

public class QuitHelperThread extends Thread {
    @Override
    public synchronized void run() {
        Logger.yellow("Waiting for maintenance to finish");
        if(BotThread.getInstance().getMaintenanceThread().isInQueue()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red("There was an error while trying to wake up");
            }
        }
        Logger.yellow("Removing the robot from the city");
        Logger.yellow("Starting temporary eliminator Thread to delete this robot");
        EliminatorThread eliminatorThread = new EliminatorThread(BotThread.getInstance().getIdentity(), true);
        eliminatorThread.start();
        try {
            eliminatorThread.join();
        } catch (InterruptedException e) {
            Logger.red("There was a problem while eliminating this robot from the network", e);
        }
    }

    public synchronized void wakeup() {
        notify();
    }
}
