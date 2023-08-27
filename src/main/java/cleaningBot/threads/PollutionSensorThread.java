package cleaningBot.threads;

import beans.BotIdentity;
import extra.Logger.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.List;

import static utilities.Variables.WAKEUP_ERROR;

public class PollutionSensorThread extends Thread {
    private int district;
    private BotIdentity botIdentity;
    private MeasurementGatheringThread measurementGatheringThread;
    private boolean brokering;
    private MqttClient client;

    public PollutionSensorThread(int district, BotIdentity identity) {
        this.district = district;
        botIdentity = identity;
        measurementGatheringThread = BotThread.getInstance().getMeasurementGatheringThread();
    }

    @Override
    public void run() {
        startBrokering();
    }

    public synchronized void startBrokering() {
        brokering = true;
        String broker = "tcp://localhost:1883";
        String clientID = MqttClient.generateClientId();
        String topic = "greenfield/pollution/" + district;
        System.out.println(topic);
        ObjectMapper mapper = new ObjectMapper();
        int qos = 1;

        try{
            client = new MqttClient(broker, clientID);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(false);
            client.connect(connectOptions);

            while(brokering) {
                try{
                    wait(15000);
                } catch (InterruptedException e) {
                    Logger.red("There was an error during the wakeup procedure");
                }
                List<Float> averages = measurementGatheringThread.getAverages();
                String payload = botIdentity.getId()
                        + "-" + mapper.writeValueAsString(System.currentTimeMillis())
                        + "-[";

                for (Float average : averages) {
                    payload = payload + String.valueOf(average) + ",";
                }

                payload = payload.replaceAll(",$", "") + "]";

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
                measurementGatheringThread.clear();
            }

            List<Float> averages = measurementGatheringThread.getAverages();
            if(!averages.isEmpty()) {
                String payload = botIdentity.getId()
                        + "-" + mapper.writeValueAsString(System.currentTimeMillis())
                        + "-[";

                for (Float average : averages) {
                    payload = payload + String.valueOf(average) + ",";
                }

                payload = payload.replaceAll(",$", "") + "]";

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
                measurementGatheringThread.clear();
            }
        } catch (MqttException e) {
            Logger.red("There was an error during the Mqtt publisher startup");
            e.printStackTrace();
        } catch (JsonMappingException e) {
            Logger.red("There was an error while translating the object into json");
        } catch (IOException e) {
            Logger.red("There was an error while handling the json parsing", e);
        }
    }

    public void closeConnection() {
        System.out.println("CLOSING CONNECTION...");
        brokering = false;

        try {
            client.disconnect(10000);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
