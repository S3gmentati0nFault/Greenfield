package cleaningBot;

import beans.BotIdentity;
import cleaningBot.threads.Bot;

public class CleaningBot {
    public static void main(String[] args) {
        Bot thread = new Bot();
        thread.run();
    }
}
