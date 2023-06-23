package cleaningBot.threads;

import java.util.Scanner;

public class InputThread extends Thread {
    BotThread botThread;

    public InputThread(BotThread botThread) {
        this.botThread = botThread;
    }

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
