package adminServer;

import beans.BotIdentity;
import beans.BotPositions;
import extra.Logger.Logger;
import extra.Position.Position;
import org.eclipse.paho.client.mqttv3.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * REST class for the Administration server. This is the REST class that
 * handles incoming connections for the administration server.
 */
@Path("admin")
public class AdminServer {
    private static List<Float> averageDistrict1;
    private static List<Float> averageDistrict2;
    private static List<Float> averageDistrict3;
    private static List<Float> averageDistrict4;

    public static void main (String[] args) {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String recievingTopic = "greenfield/pollution/#";

        averageDistrict1 = new ArrayList<>();
        averageDistrict2 = new ArrayList<>();
        averageDistrict3 = new ArrayList<>();
        averageDistrict4 = new ArrayList<>();

        int qos = 1;

        try {
            client = new MqttClient(broker, clientId);

            MqttConnectOptions connOpts = new MqttConnectOptions();

            connOpts.setCleanSession(false);

            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {

                public void messageArrived(String recievingTopic, MqttMessage message)
                        throws MqttException {

                    String[] stringAverages = (new String(message.getPayload())).split("-");
                    int district = Integer.parseInt(String.valueOf(recievingTopic.charAt(recievingTopic.length() - 1)));
                    System.out.println(district);
                    for (String stringAverage : stringAverages) {
                        if(!stringAverage.equals("")){
                            Float average = Float.parseFloat(stringAverage);
                            switch(district) {
                                case 1:
                                    averageDistrict1.add(average);
                                    break;
                                case 2:
                                    averageDistrict2.add(average);
                                    break;
                                case 3:
                                    averageDistrict3.add(average);
                                    break;
                                case 4:
                                    averageDistrict4.add(average);
                                    break;
                                default:
                                    Logger.red("There was an error while adding the data to memory");
                            }
                        }
                    }

                }

                public void connectionLost(Throwable cause) {
                    Logger.red(clientId + " Connectionlost! cause:" + cause.getMessage()+ "-  Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });
            client.subscribe(recievingTopic,qos);

        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    /**
     * This function handles the addition of a new bot to the network.
     * @param identity the identity of the bot, this is an object containing
     *                 the bot's ID
     *                 the bot's port
     *                 the bot's ip address
     * @return The return value is an HTTP response which is either 200 ok or
     * Error
     */
    @Path("join")
    @POST
    @Consumes({"application/json"})
    public Response joinBot(BotIdentity identity) {
        Position botPosition = BotPositions.getInstance().joinBot(identity);
        if(botPosition == null){
            return Response.serverError().build();
        }
        else{
            String jsonString = BotPositions.getInstance().jsonBuilder(botPosition);
            return Response.ok(jsonString).build();
        }
    }

    /**
     * This function returns the set of bots registered within the city.
     * @return The return value is an HTTP response which is only 200 ok.
     */
    @Path("bots")
    @GET
    public Response getBots() {
        System.out.println(BotPositions.getInstance().getBotPositioning());
        return Response.ok().build();
    }

    /**
     * This function deletes a bot from the city.
     * @return The return function is a value that can be 200 ok if the bot was actually
     * present in the city, and it was correctly deleted from the administration server.
     */
    @Path("remove")
    @DELETE
    @Consumes({"application/json"})
    public Response deleteBot(BotIdentity botIdentity) {
        Logger.blue("DELETE");
        if(!BotPositions.getInstance().deleteBot(botIdentity)) {
            return Response.noContent().build();
        }
        return Response.ok().build();
    }

    @Path("measurements/{id}/{number}")
    @GET
    public Response getAvgMeasurement(@PathParam("id") int robotID,
                                      @PathParam("number") int numberOfMeasurements) {
        return Response.ok().build();
    }

    public static List<Float> getAverageDistrict1() {
        return averageDistrict1;
    }

    public static List<Float> getAverageDistrict2() {
        return averageDistrict2;
    }

    public static List<Float> getAverageDistrict3() {
        return averageDistrict3;
    }

    public static List<Float> getAverageDistrict4() {
        return averageDistrict4;
    }
}
