package beans;

import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Position.Position;
import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
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

//    public synchronized boolean deleteBot(BotIdentity identity) {
//        Logger.notice("REMOVAL");
//        if(!botPositioning.containsKey(identity)){
//            Logger.notice("The bot " +
//                    identity.toString() +
//                    " is not present in the city");
//            return false;
//        }
//        else{
//            Logger.notice("Removing node " + identity.toString());
//            botPositioning.remove(identity);
//            return true;
//        }
//    }

    public String jsonBuilder(Position botPosition) {
        String json = "";
        try{
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(botPosition);
            json = json + "-[";
            Set<BotIdentity> keys = botPositioning.keySet();
            for (BotIdentity bot: keys) {
                json = json + mapper.writeValueAsString(bot) + ",";
            }
            json = json.replaceAll(",$", "") + "]";
            Logger.notice(json);
        }
        catch(IOException e){
            Logger.error("There was an error while constructing the response");
        }
        return json;
    }
}
