package cleaningBot.threads;

import cleaningBot.BotUtilities;
import extra.Logger.Logger;

public class QuitHelperThread extends Thread {
    @Override
    public synchronized void run() {
        if(BotThread.getInstance().getMaintenanceThread().isInQueue()) {
            Logger.blue("Waiting for maintenance to finish");
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red("There was an error while trying to wake up");
            }
        }
        Logger.cyan("Removing the robot from the city");
        Logger.yellow("Starting temporary eliminator Thread to delete this robot");
        EliminatorThread eliminatorThread = new EliminatorThread(BotThread.getInstance().getIdentity(), true);
        eliminatorThread.start();
    }
}
