package beans;

import extra.CustomRandom.CustomRandom;
import extra.Position.Position;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "city-map")
public class BotPositions {
    private Map<BotIdentity, Position> botPositioning;
    private static BotPositions instance;

    public BotPositions() {
        botPositioning = new HashMap<>();
    }

    public synchronized static BotPositions getInstance() {
        if(instance == null){
            instance = new BotPositions();
        }
        return instance;
    }

    public Map<BotIdentity, Position> getBotPositioning() {
        return botPositioning;
    }

    public void setBotPositioning(Map<BotIdentity, Position> botPositioning) {
        this.botPositioning = botPositioning;
    }

    public synchronized Position joinBot(BotIdentity identity) {
        Position pos = new Position(CustomRandom.getInstance().rnInt(9), CustomRandom.getInstance().rnInt(9));
        if(botPositioning.putIfAbsent(identity, pos) == null){
            return pos;
        }
        return null;
    }

    public String jsonBuilder(Position botPosition) {
        String json = "\"position\": {" +
                "\"x\":" + botPosition.getX() +
                ", \"y\": " + botPosition.getY() +
                "}," +
                "\"otherRobots\": [";

        Set<BotIdentity> keys = botPositioning.keySet();
        for (BotIdentity bot: keys) {
            Position value = botPositioning.get(bot);
            json = json +
                    ", {" +
                    "\"id\": " + bot.getId() +
                    ", \"port\": " + bot.getPort() +
                    ", \"ip\": " + bot.getIp() +
                    "}";
        }
        return json + "]";
    }
}
