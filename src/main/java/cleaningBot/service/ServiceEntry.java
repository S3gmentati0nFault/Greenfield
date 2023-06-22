package cleaningBot.service;

public class ServiceEntry {
    private BotServices botServices;
    private long timestamp;

    public ServiceEntry(BotServices botServices, long timestamp) {
        this.botServices = botServices;
        this.timestamp = timestamp;
    }

    public BotServices getBotServices() {
        return botServices;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
