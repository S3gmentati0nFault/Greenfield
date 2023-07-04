package beans;

import java.util.List;

public class AverageList {
    private long timestamp;
    private List<Float> averages;
    private int size, identity;

    public AverageList() {}

    public AverageList(int size, int identity, long timestamp, List<Float> averages) {
        this.identity = identity;
        this.timestamp = timestamp;
        this.averages = averages;
        this.size = size;
    }

    public int getIdentity() {
        return identity;
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
