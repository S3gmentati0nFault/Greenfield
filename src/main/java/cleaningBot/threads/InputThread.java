package cleaningBot.threads;

import extra.Logger.Logger;

import java.util.ConcurrentModificationException;
import java.util.Scanner;

/**
 * An input thread class used to get the input from the user
 */
public class InputThread extends Thread {
    private QuitHelperThread quitHelperThread;
    private FixHelperThread fixHelperThread;
    private boolean quitting, maintenanceRequested;

    /**
     * Generic public constructor
     */
    public InputThread() {
        quitting = false;
        maintenanceRequested = false;
    }

    /**
     * Override of the run method that starts the reading cycle up
     */
    @Override
    public void run() {
        while(true){
            Scanner keyboard = new Scanner(System.in);
            String input = keyboard.next();

            if(input.length() != 0) {
                System.out.println(input);
            }

            if (input.equals("GET")) {
                System.out.println("-> " + BotThread.getInstance().getIdentity());
                BotThread.getInstance().printOtherBots();
            }
            else if(input.equals("FIX")) {
                synchronized(this) {
                    if(maintenanceRequested) {
                        Logger.red("Maintenance has already been requested, wait please...");
                    }
                    else{
                        maintenanceRequested = true;
                        Logger.yellow("Requesting immediate maintenance...");
                        fixHelperThread = new FixHelperThread(this);
                        fixHelperThread.start();
                    }
                }
            }
            else if(input.equals("QUIT")) {
                if(quitting) {
                    Logger.red("Already quitting the program, wait please...");
                }
                quitting = true;
                Logger.yellow("Initiating the quit procedure...");
                quitHelperThread = new QuitHelperThread();
                quitHelperThread.start();
            }
            else{
                Logger.red("Input could not be recognized");
            }
        }
    }

    public synchronized void maintenanceDone() {
        maintenanceRequested = false;
    }

    public synchronized void wakeupHelper() {
        if(quitting){
            quitHelperThread.wakeup();
        }
    }
}