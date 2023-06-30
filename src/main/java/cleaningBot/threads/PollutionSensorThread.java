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

public class PollutionSensorThread extends Thread {
    private int district;
    private BotIdentity botIdentity;
    private MeasurementGatheringThread measurementGatheringThread;

    public PollutionSensorThread(int district, BotIdentity identity) {
        this.district = district;
        botIdentity = identity;
    }

    @Override
    public void run() {
        Logger.yellow("Starting the measurement gathering thread");
        measurementGatheringThread = new MeasurementGatheringThread();
        measurementGatheringThread.start();
        startBrokering();
    }

    public synchronized void startBrokering() {
        MqttClient client;
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

            while(true) {
                Logger.yellow("Waiting to gather some data");
                try{
                    wait(15000);
                } catch (InterruptedException e) {
                    Logger.red("There was an error during the wakeup procedure");
                }
                Logger.yellow("Preparing to send data");
                List<Float> averages = measurementGatheringThread.getAverages();
                String payload = mapper.writeValueAsString(botIdentity)
                        + "-" + mapper.writeValueAsString(System.currentTimeMillis())
                        + "-[";

                for (Float average : averages) {
                    payload = payload + String.valueOf(average) + ",";
                }

                payload = payload.replaceAll(",$", "") + "]";
                System.out.println(payload);

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
}
