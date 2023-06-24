package cleaningBot.service;

/**
 * WaitingThread class
 */
public class WaitingThread {
    private BotServices botServices;
    private long timestamp;

    /**
     * Basic constructor
     * @param botServices The identity of the waiting thread
     * @param timestamp the timestamp of its request
     */
    public WaitingThread(BotServices botServices, long timestamp) {
        this.botServices = botServices;
        this.timestamp = timestamp;
    }

    /**
     * Getter for the waiting thread identity
     */
    public BotServices getBotServices() {
        return botServices;
    }

    /**
     * Getter for its request timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
