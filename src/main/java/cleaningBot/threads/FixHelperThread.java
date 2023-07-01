package cleaningBot.threads;

import exceptions.AlreadyOnMaintenanceException;
import extra.Logger.Logger;

public class FixHelperThread extends Thread{
    @Override
    public void run() {
        try{
            BotThread.getInstance().getMaintenanceThread().requestMaintenance();
        } catch (AlreadyOnMaintenanceException e) {
            Logger.red("The thread is already in maintenance!", e);
        }
    }
}
