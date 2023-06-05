package beans;

import extra.CustomRandom.CustomRandom;
import extra.Position.Position;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public synchronized String joinBot(BotIdentity identity) {
        Position pos = new Position(CustomRandom.getInstance().rnInt(9), CustomRandom.getInstance().rnInt(9));
        if(botPositioning.putIfAbsent(identity, pos) == null){
            return "The bot has been placed in " + pos.getX() + ", " + pos.getY();
        }
        return "The bot with id " + identity.getId() + " is already roaming the city";
    }
}
