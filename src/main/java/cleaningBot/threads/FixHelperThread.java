package cleaningBot.threads;

import exceptions.AlreadyOnMaintenanceException;
import extra.AtomicFlag.AtomicFlag;
import extra.Logger.Logger;

public class FixHelperThread extends Thread {
    private AtomicFlag maintenanceRequested;

    public FixHelperThread(AtomicFlag maintenanceRequested) {
        this.maintenanceRequested = maintenanceRequested;
    }

    public void run() {
        try {
            BotThread.getInstance().getMaintenanceThread().doMaintenance();
        } catch (AlreadyOnMaintenanceException e) {
            Logger.red("The thread is already in maintenance!", e);
        }
        maintenanceRequested.setFlag(false);
    }
}
