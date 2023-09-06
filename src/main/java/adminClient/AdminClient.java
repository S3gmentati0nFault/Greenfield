package adminClient;

import beans.BotIdentity;
import cleaningBot.BotUtilities;
import extra.Logger.Logger;
import utilities.Variables;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Queue;

import static cleaningBot.BotUtilities.buildConnection;
import static utilities.Variables.NUMBER_OF_DISTRICTS;

public class AdminClient {

    public static void main(String[] args) {
        int command = -1;
        Logger.blue("Through this interface it's possible to:");
        Logger.blue("\t- Get the list of robots currently roaming in the city");
        Logger.blue("\t- Get the distribution of the robots currently present in the system");
        Logger.blue("\t- Get the average of the last n air pollution measurements");
        Logger.blue("\t- Get the average of the last air pollution measurements sent to the " +
                "server between a beginning time t1 and an ending time t2");
        Logger.blue("\t- Get the number of entries currently memorized in the data structure");
        Logger.blue("Choice: [0, 1, 2, 3, 4]");

        while(true) {
            BufferedReader bf =
                    new BufferedReader(new InputStreamReader(System.in));

            try {
                command = Integer.parseInt(bf.readLine());
            } catch (IOException e) {
                Logger.red("There was an error while trying to read");
            }

            switch(command) {
                case 0:
                    Logger.yellow("Gathering information about the robots roaming the city");
                    getRobots();
                    break;
                case 1:
                    Logger.yellow("Gathering information about the distribution of the robots roaming the city");
                    getDistribution();
                    break;
                case 2:
                    Logger.yellow("Getting the average of the last n air pollution measurements");
                    getPollutionMeasurements(bf);
                    break;
                case 3:
                    Logger.yellow("Getting the average of the last n air pollution measurements in a timeframe");
                    getPollutionMeasurementTimeframe(bf);
                    break;
                case 4:
                    Logger.yellow("Getting the number of elements currently present in the data structure");
                    getNumbers();
                    break;
                default:
                    Logger.red("The given input cannot be recognized");
            }
            Logger.blue("Choice: [0, 1, 2, 3, 4]");
        }
    }

    private static void getDistribution() {
        HttpURLConnection connection =
                buildConnection("GET", "http://" +
                        Variables.HOST+":" + Variables.PORT + "/admin/bots");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(false);

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            return;
        }

        List<BotIdentity> bots = null;
        try{
            String responseLine = br.readLine();
            bots = new ObjectMapper().readValue(responseLine, new TypeReference<List<BotIdentity>>(){});
        } catch (IOException e) {
            Logger.red("There was an error while parsing information sent from the server");
            return;
        }

        List<Queue<BotIdentity>> distribution = BotUtilities.distributionCalculator(bots);

        for(int i = 0; i < NUMBER_OF_DISTRICTS; i++) {
            System.out.println("DISTRICT " + (i + 1) + "\t< " + distribution.get(i).size() + " >");
            for (BotIdentity botIdentity : distribution.get(i)) {
                System.out.println(botIdentity);
            }
        }
    }

    private static void getPollutionMeasurementTimeframe(BufferedReader bf) {
        Logger.blue("To get the pollution measurements average it's required to input the " +
                "robot identifier and the number of measurements");
        long t1, t2;
        try{
            Logger.blue("Starting time:");
            t1 = Long.parseLong(bf.readLine());
            Logger.blue("End time:");
            t2 = Long.parseLong(bf.readLine());
        } catch (IOException e) {
            Logger.red("There was an error while trying to read from user input");
            return;
        }

        HttpURLConnection connection =
                buildConnection("GET", "http://"
                        + Variables.HOST+":" + Variables.PORT + "/admin/measurements/timestamp/"
                        + t1 + "/" + t2);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(false);

        try{
            if(connection.getResponseCode() == 500) {
                Logger.red("There was a server error");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            e.printStackTrace();
            return;
        }

        try{
            Logger.blue("The average of the last measurements between " + t1 + " and " + t2
                    + " is >> " + br.readLine().toString());
        } catch (IOException e) {
            Logger.red("There was an error while parsing information sent from the server");
            return;
        }
    }

//    TODO
//    >> FLAVOUR :: LOGICA-GIALLO <<
//    CAMBIARE LE SEGUENTI FUNZIONI PERCHÈ ACCETTINO MISURE IN SECONDI COME PUNTO DI PARTENZA E PUNTO DI ARRIVO PER IL
//    POLLING DELLE INFORMAZIONI
    private static void getNumbers() {
        HttpURLConnection connection =
                buildConnection("GET", "http://" +
                        Variables.HOST+":" + Variables.PORT + "/admin/number");

        connection.setRequestProperty("Accept", "plain/text");

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            return;
        }

        try{
            Logger.blue("The number of entries currently in the data structure is >> " + br.readLine().toString());
        } catch (IOException e) {
            Logger.red("There was an error while parsing information sent from the server");
            return;
        }
    }

    private static void getPollutionMeasurements(BufferedReader bf) {
        Logger.blue("To get the pollution measurements average it's required to input the " +
                "robot identifier and the number of measurements");
        int id = -1;
        int number = -1;
        try{
            Logger.blue("identifier");
            id = Integer.parseInt(bf.readLine());
            Logger.blue("number of measurements");
            number = Integer.parseInt(bf.readLine());
        } catch (IOException e) {
            Logger.red("There was an error while trying to read from user input");
            return;
        }

        HttpURLConnection connection =
                buildConnection("GET", "http://"
                        + Variables.HOST+":" + Variables.PORT + "/admin/measurements/bot/"
                        + id + "/" + number);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(false);

        try{
            if(connection.getResponseCode() == 500) {
                Logger.red("There was a server error");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            e.printStackTrace();
            return;
        }

        try{
            Logger.blue("The average of the last " + number + " measurements is >> " + br.readLine().toString());
        } catch (IOException e) {
            Logger.red("There was an error while parsing information sent from the server");
            return;
        }
    }

    private static void getRobots() {
        HttpURLConnection connection =
                buildConnection("GET", "http://" +
                        Variables.HOST+":" + Variables.PORT + "/admin/bots");

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(false);

        BufferedReader br = null;
        try{
            br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        }catch(IOException e){
            Logger.red("It was not possible to initialize the BufferedReader");
            return;
        }

        List<BotIdentity> bots = null;
        try{
            String responseLine = br.readLine();
            bots = new ObjectMapper().readValue(responseLine, new TypeReference<List<BotIdentity>>(){});
        } catch (IOException e) {
            Logger.red("There was an error while parsing information sent from the server");
            return;
        }

        if(!bots.isEmpty()) {
            Logger.green(bots.size() + " are roaming the city!");
            for (BotIdentity bot : bots) {
                Logger.yellow("< " + String.valueOf(bot.getId()) + " >> << "
                        + String.valueOf(bot.getPort()) + " >> << " + bot.getIp() + " >> << " + bot.getPosition() + " >");
            }
        }
        else{
            Logger.red("The set of bots is empty!");
        }
    }
}
