package beans;

import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Position.Position;
import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.*;

/**
 * BotPositions bean which simulates the city, it stores the position of all
 * the bots into an HashMap.
 */
@XmlRootElement(name = "city-map")
public class BotPositions {
    private Map<BotIdentity, Position> botPositioning;
    private static BotPositions instance;

    /**
     * Public default constructor which builds the HashMap.
     */
    public BotPositions() {
        botPositioning = new HashMap<>();
    }

    /**
     * The city is a Singleton class thus the instance can be accessed only
     * via the instance variable.
     */
    public static BotPositions getInstance() {
        if(instance == null){
            instance = new BotPositions();
        }
        return instance;
    }

    /**
     * Getter for the bot position. Getter for the bot position, since reading
     * is not a problem in a concurrent system the operation is not
     * synchronized.
     */
    public Map<BotIdentity, Position> getBotPositioning() {
        return botPositioning;
    }

    /**
     * Method to add a bot to the city map. With this method a bot can be added to
     * the city, its position is generated randomly.
     * @return The method will return either the position or null if the bot was already
     * present in the system.
     */
    public Position joinBot(BotIdentity identity) {
        Position pos = new Position(
                CustomRandom.getInstance().rnInt(9),
                CustomRandom.getInstance().rnInt(9)
        );

        if(botPositioning.putIfAbsent(identity, pos) == null){
            return pos;
        }
        return null;
    }

    /**
     * Method that removes a robot from the city. Synchronized method to remove a function
     * from the city, it is synchronized because I don't want anything to happen to the
     * distributed data structure in case multiple requests for deletion happen at the
     * same time.
     * @param identity The identity of the robot.
     * @return It returns true if the deletion operation went well, false otherwise.
     */
    public boolean deleteBot(BotIdentity identity) {
        Logger.blue("deletion");

        if(botPositioning.containsKey(identity)){
            botPositioning.remove(identity);
            return true;
        }
        else {
            Logger.red("The indicated bot is not present in the data structure");
        }
        return false;
    }

    /**
     * Method to create a json string to be sent to the bot. Method that creates a json
     * string starting from the bot position and the identities of all the bots in the
     * system.
     * @param botPosition the position of the bot inside the system.
     * @return A json string containing the bot position and the identity of all the bots
     * present inside the system enclosed in an array. The position object and the json
     * array are separated via a "-" character.
     */
    public String jsonBuilder(Position botPosition) {
        String json = "";
        try{
            ObjectMapper mapper = new ObjectMapper();
            if(botPosition != null){
                json = mapper.writeValueAsString(botPosition);
                json = json + "-[";
            }
            else{
                json = json + "[";
            }
            Set<BotIdentity> keys = botPositioning.keySet();
            for (BotIdentity bot: keys) {
                json = json + mapper.writeValueAsString(bot) + ",";
            }
            json = json.replaceAll(",$", "") + "]";
            Logger.blue(json);
        }
        catch(IOException e){
            Logger.red("There was an error while constructing the response");
        }
        return json;
    }
}
