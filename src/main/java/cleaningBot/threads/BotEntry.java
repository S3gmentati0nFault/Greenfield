package cleaningBot.threads;

import java.util.Objects;

public class BotEntry {
    private int timestamp, id;
    public BotEntry(int timestamp, int id) {
        this.timestamp = timestamp;
        this.id = id;
    }
    public int getTimestamp() {
        return timestamp;
    }
    public int getId() {
        return id;
    }
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotEntry botEntry = (BotEntry) o;
        return (timestamp == botEntry.getTimestamp()) && (id == botEntry.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }

    @Override
    public String toString() {
        return "BotEntry{" +
                "timestamp=" + timestamp +
                ", id=" + id +
                '}';
    }
}