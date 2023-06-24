package cleaningBot.threads;

import java.util.Scanner;

/**
 * An input thread class used to get the input from the user
 */
public class InputThread extends Thread {
    BotThread botThread;

    /**
     * Generic public constructor
     */
    public InputThread(BotThread botThread) {
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

            if (input.equals("GET")) {
                System.out.println("-> " + botThread.getIdentity());
                botThread.printOtherBots();
            }
        }
    }
}
