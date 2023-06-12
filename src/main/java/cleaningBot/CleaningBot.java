package cleaningBot;

import cleaningBot.threads.Bot;

public class CleaningBot {
    public static void main(String[] args) {
        Bot botThread = new Bot();
        botThread.start();
    }
}
