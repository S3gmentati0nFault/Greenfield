package adminServer;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import extra.Logger.Logger;
import org.omg.CORBA.TIMEOUT;
import utilities.Variables;
import beans.AverageList;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class AdminServer {
    private static Stack<AverageList> averageLists;

    public static void main(String[] args) {
        Logger.green("adminServerJoinTest");
        HttpServer server = null;
        try{
            server = HttpServerFactory.create("http://"+ Variables.HOST+":"+Variables.PORT+"/");
        } catch (IOException e) {
            Logger.red("There was a fatal error trying to start the server up", e);
            return;
        }
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: http://"+Variables.HOST+":"+Variables.PORT);

        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String recievingTopic = "greenfield/pollution/#";
        int qos = 1;

        try {
            client = new MqttClient(broker, clientId);

            MqttConnectOptions connOpts = new MqttConnectOptions();

            connOpts.setCleanSession(false);

            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected - Thread PID: "
                    + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {
                public void messageArrived(String recievingTopic, MqttMessage message)
                        throws MqttException, InterruptedException {
                    String[] messageComponents = new String(message.getPayload()).split("-");

                    int senderIdentity = -1;
                    senderIdentity = Integer.parseInt(messageComponents[0]);

                    Long timestamp = Long.parseLong(messageComponents[1]);

                    System.out.println("------MESSAGE-------"
                            + "\n\t - TOPIC - " + recievingTopic
                            + "\n\t - IDENTITY - " + senderIdentity
                            + "\n\t - TIMESTAMP - " + timestamp
                            + "\n" + messageComponents[2]
                            + "\n----------------------");

                    List<Float> averageList = null;
                    try {
                        averageList = new ObjectMapper().readValue(messageComponents[2],
                                new TypeReference<List<Float>>() {
                                });
                    } catch (JsonMappingException e) {
                        Logger.red("There was an error while handling json mapping", e);
                    } catch (JsonParseException e) {
                        Logger.red("There was an error while handling json parsing", e);
                    } catch (IOException e) {
                        Logger.red("There was an error while reading from stream", e);
                    }

                    AverageList al;
                    if (senderIdentity != -1 && averageList != null) {
                        al = new AverageList(averageList.size(), senderIdentity,
                                timestamp, averageList);
                    } else {
                        Logger.red("Something went terribly wrong");
                        return;
                    }

                    getStack().push(al);
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:"
                            + cause.getMessage() + "-  Thread PID: "
                            + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery complete");
                }
            });
            System.out.println(clientId + " Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(recievingTopic,qos);
            System.out.println(clientId + " Subscribed to topic : " + recievingTopic);

        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public synchronized static Stack<AverageList> getStack() {
        if(averageLists == null) {
            averageLists = new Stack<>();
        }
        return averageLists;
    }
}
