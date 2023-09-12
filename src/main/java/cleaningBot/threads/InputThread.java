package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import extra.AtomicFlag.AtomicFlag;
import extra.Logger.Logger;

import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import static utilities.Variables.NUMBER_OF_DISTRICTS;

/**
 * An input thread class used to get the input from the user
 */
public class InputThread extends Thread {
    private QuitHelperThread quitHelperThread;
    private FixHelperThread fixHelperThread;
    AtomicFlag quitting, maintenanceRequested;

    /**
     * Generic public constructor
     */
    public InputThread() {
        quitting = new AtomicFlag(false);
        maintenanceRequested = new AtomicFlag(false);
    }

    /**
     * Override of the run method that starts the reading cycle up
     */
    @Override
    public void run() {
        while (true) {
            Scanner keyboard = new Scanner(System.in);
            String input = keyboard.next();

            if (input.equals("GET")) {
                System.out.println("-> " + BotThread.getInstance().getIdentity());
                BotThread.getInstance().printOtherBots();
            } else if (input.equals("OPEN")) {
                BotThread.getInstance().printOpenComms();
            } else if (input.equals("FIX")) {
                if (maintenanceRequested.isFlag()) {
                    Logger.red("Maintenance has already been requested, wait please...");
                } else {
                    maintenanceRequested.setFlag(true);
                    Logger.cyan("Requesting immediate maintenance...");
                    fixHelperThread = new FixHelperThread(this);
                    fixHelperThread.start();
                }
            } else if (input.equals("DIST")) {
                List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
                fleetSnapshot.add(BotThread.getInstance().getIdentity());
                List<Queue<BotIdentity>> distribution = BotUtilities.distributionCalculator(fleetSnapshot);
                for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
                    System.out.println("DISTRICT " + (i + 1) + "\t< " + distribution.get(i).size() + " >");
                    for (BotIdentity botIdentity : distribution.get(i)) {
                        System.out.println(botIdentity);
                    }
                }
            } else if (input.equals("QUIT")) {
                if (quitting.isFlag()) {
                    Logger.red("Already quitting the program, wait please...");
                }
                else {
                    quitting.setFlag(true);
                    Logger.cyan("Initiating the quit procedure...");
                    quitHelperThread = new QuitHelperThread();
                    quitHelperThread.start();
                }
            } else {
                Logger.red("Input could not be recognized");
            }
        }
    }


    public QuitHelperThread getQuitHelperThread() {
        return quitHelperThread;
    }

    public AtomicFlag getMaintenanceRequested() {
        return maintenanceRequested;
    }
}