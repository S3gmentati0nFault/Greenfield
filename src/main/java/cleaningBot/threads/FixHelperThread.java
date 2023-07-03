package cleaningBot.threads;

import exceptions.AlreadyOnMaintenanceException;
import extra.Logger.Logger;

public class FixHelperThread extends Thread{
    private InputThread inputThread;

    public FixHelperThread(InputThread inputThread) {
        this.inputThread = inputThread;
    }

    @Override
    public void run() {
        try{
            BotThread.getInstance().getMaintenanceThread().doMaintenance();
        } catch (AlreadyOnMaintenanceException e) {
            Logger.red("The thread is already in maintenance!", e);
        }
        inputThread.maintenanceDone();
    }
}
