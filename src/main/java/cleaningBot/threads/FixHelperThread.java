package cleaningBot.threads;

import exceptions.AlreadyOnMaintenanceException;
import extra.Logger.Logger;

public class FixHelperThread extends Thread{
    public FixHelperThread(InputThread inputThread) {}

    @Override
    public void run() {
        try{
            BotThread.getInstance().getMaintenanceThread().doMaintenance();
        } catch (AlreadyOnMaintenanceException e) {
            Logger.red("The thread is already in maintenance!", e);
        }
        BotThread.getInstance().getInputThread().getMaintenanceRequested().setFlag(false);
    }
}
