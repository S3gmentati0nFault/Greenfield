package beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * The BotIdentity bean is used throughout the system to memorize the
 * fundamental informations of a bot, which are:
 * Its id
 * Its ip address
 * Its port
 */
@XmlRootElement(name = "bot-identity")
public class BotIdentity {
    private int id, port;
    private String ip;

    /**
     * The bot identity constructor.
     * @param id An integer value, there are no equal ids in the system.
     * @param port An integer value, there are no equal ports in the system.
     * @param ip A string value, every ip address in this specific project is
     *           hardcoded to use localhost.
     */
    public BotIdentity(int id, int port, String ip) {
        this.id = id;
        this.port = port;
        this.ip = ip;
    }

    /**
     * Id getter.
     */
    public int getId() {
        return id;
    }

    /**
     * Port getter.
     */
    public int getPort() {
        return port;
    }

    /**
     * Ip getter.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Equals redefinition. In this override of the equals method I
     * consider two bots to be equal if and only if their ids coincide.
     */
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

    /**
     * hashCode override. In this override of the hashCode method I use only
     * the id to build the hashcode (I use the hashcode in the implementation
     * of the BotPositions bean).
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * toString override.
     */
    @Override
    public String toString() {
        return "BotIdentity{" +
                "id=" + id +
                ", port=" + port +
                ", ip='" + ip + '\'' +
                '}';
    }
}
