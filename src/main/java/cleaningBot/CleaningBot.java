package cleaningBot;

import beans.BotIdentity;
import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;
import extra.Position.Position;
import extra.Variables;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class CleaningBot extends BotIdentity {
    public static void main(String[] args) {
        List<CleaningBot> otherBots;
        Position pos;
        BotIdentity id = new BotIdentity(CustomRandom.getInstance().rnInt(100), CustomRandom.getInstance().rnInt(65534), "localhost");

        try{
            startNewBot(id);
        }
        catch(Exception e){

        }
    }

    public static void startNewBot(BotIdentity id) {
        URL requestURL;
        try{
            requestURL = new URL("http://"+ Variables.HOST+":"+ Variables.PORT+"/admin/join");

            HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String jsonString = "{\"id\": " + id.getId() + ", \"port\": " + id.getPort() + ", \"ip\": \"" + id.getIp() + "\"}";

            OutputStream os = connection.getOutputStream();
            byte[] input = jsonString.getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = br.readLine().toString();
            Logger.notice(responseLine);
        }
        catch(MalformedURLException e){
            Logger.error("The URL was not well formed");
        } catch (IOException e) {
            Logger.error("Something went wrong during while opening the connection");
        }
    }
}
