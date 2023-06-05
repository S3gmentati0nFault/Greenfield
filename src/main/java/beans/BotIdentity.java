package beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "bot-identity")
public class BotIdentity {
    private int id, port;
    private String ip;

    public BotIdentity() {}

    public BotIdentity(int id, int port, String ip) {
        this.id = id;
        this.port = port;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BotIdentity that = (BotIdentity) other;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BotIdentity{" +
                "id=" + id +
                ", port=" + port +
                ", ip='" + ip + '\'' +
                '}';
    }
}
