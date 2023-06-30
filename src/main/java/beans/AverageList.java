package beans;

import java.util.List;

public class AverageList {
    private BotIdentity botIdentity;
    private long timestamp;
    private List<Float> averages;
    private int size;

    public AverageList() {}

    public AverageList(int size, BotIdentity botIdentity, long timestamp, List<Float> averages) {
        this.botIdentity = botIdentity;
        this.timestamp = timestamp;
        this.averages = averages;
        this.size = size;
    }

    public BotIdentity getBotIdentity() {
        return botIdentity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Float> getAverages() {
        return averages;
    }

    public int getSize() {
        return size;
    }
}
