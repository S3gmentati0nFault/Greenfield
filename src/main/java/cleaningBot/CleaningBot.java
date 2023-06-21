package cleaningBot;

import cleaningBot.threads.BotThread;

public class CleaningBot {
    public static void main(String[] args) {
        BotThread botThread = new BotThread();
        botThread.start();
    }
}
