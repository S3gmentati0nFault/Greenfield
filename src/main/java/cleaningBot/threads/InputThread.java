package cleaningBot.threads;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import exceptions.AlreadyOnMaintenanceException;
import extra.AtomicFlag.AtomicFlag;
import extra.Logger.Logger;

import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import static utilities.Variables.NUMBER_OF_DISTRICTS;
import static utilities.Variables.WAKEUP_ERROR;

/**
 * An input thread class used to get the input from the user
 */
public class InputThread extends Thread {
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
                fixCommand();
            } else if (input.equals("DIST")) {
                distCommand();
            } else if (input.equals("QUIT")) {
                quitCommand();
            } else {
                Logger.red("Input could not be recognized");
            }
        }
    }

    private void quitCommand() {
        if (quitting.isFlag()) {
            Logger.red("Already quitting the program, wait please...");
        }
        else {
            quitting.setFlag(true);
            Logger.cyan("Initiating the quit procedure...");
            synchronized(this) {
                if (BotThread.getInstance().getMaintenanceThread().isInQueue()) {
                    Logger.blue("Waiting for maintenance to finish");
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Logger.red(WAKEUP_ERROR, e);
                    }
                }
            }
            Logger.cyan("Removing the robot from the city");
            Logger.yellow("Starting temporary eliminator Thread to delete this robot");
            EliminatorThread eliminatorThread = new EliminatorThread(BotThread.getInstance().getIdentity(), true);
            eliminatorThread.start();
        }
    }

    private static void distCommand() {
        List<BotIdentity> fleetSnapshot = BotThread.getInstance().getOtherBots().getCopy();
        fleetSnapshot.add(BotThread.getInstance().getIdentity());
        List<Queue<BotIdentity>> distribution = BotUtilities.distributionCalculator(fleetSnapshot);
        for (int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            System.out.println("DISTRICT " + (i + 1) + "\t< " + distribution.get(i).size() + " >");
            for (BotIdentity botIdentity : distribution.get(i)) {
                System.out.println(botIdentity);
            }
        }
    }

    private void fixCommand() {
        if (maintenanceRequested.isFlag()) {
            Logger.red("Maintenance has already been requested, wait please...");
        } else {
            maintenanceRequested.setFlag(true);
            Logger.cyan("Requesting immediate maintenance...");
            FixHelperThread fixHelperThread = new FixHelperThread(maintenanceRequested);
            fixHelperThread.start();
        }
    }
}