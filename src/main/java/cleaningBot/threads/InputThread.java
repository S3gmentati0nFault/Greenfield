package cleaningBot.threads;

import extra.Logger.Logger;

import java.util.Scanner;

/**
 * An input thread class used to get the input from the user
 */
public class InputThread extends Thread {
    private BotThread botThread;
    private QuitHelperThread quitHelperThread;
    private boolean quitting, maintenanceRequested;

    /**
     * Generic public constructor
     */
    public InputThread(BotThread botThread) {
        quitting = false;
        maintenanceRequested = false;
        this.botThread = botThread;
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
                System.out.println("-> " + botThread.getIdentity());
                botThread.printOtherBots();
            }
            else if(input.equals("FIX")) {
                if(maintenanceRequested) {
                    Logger.red("Maintenance has already been requested, wait please...");
                }
                else{
                    maintenanceRequested = true;
                    Logger.yellow("Requesting immediate maintenance...");
                    FixHelperThread fixHelperThread = new FixHelperThread();
                    fixHelperThread.start();
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

    public void wakeUpHelper() {
        if(quitHelperThread != null){
            quitHelperThread.wakeup();
        }
    }
}