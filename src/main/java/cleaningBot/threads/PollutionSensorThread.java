package cleaningBot.threads;

import beans.BotIdentity;
import extra.Logger.Logger;
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
    private boolean brokering;
    private MqttClient client;

    public PollutionSensorThread(int district, BotIdentity identity) {
        this.district = district;
        botIdentity = identity;
    }

    @Override
    public void run() {
        measurementGatheringThread = BotThread.getInstance().getMeasurementGatheringThread();
        startBrokering();
    }

    public void startBrokering() {
        brokering = true;
        String broker = "tcp://localhost:1883";
        String clientID = MqttClient.generateClientId();
        String topic = "greenfield/pollution/" + district;
        System.out.println(topic);
        ObjectMapper mapper = new ObjectMapper();
        int qos = 1;

//        TODO
//        >> FLAVOUR :: CONSEGNA-ROSSO <<
//        FIXARE IL COMPORTAMENTO DEL SISTEMA DI GESTIONE DELLE MISURAZIONI
        try{
            client = new MqttClient(broker, clientID);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(false);
            client.connect(connectOptions);

            while(brokering) {
                try {
                    System.out.println("In attesa di un nuovo ciclo di lettura");
                    sleep(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(">> Getting averages <<");
                List<Float> averages = measurementGatheringThread.getAverages();
                StringBuilder payload = new StringBuilder(botIdentity.getId()
                        + "-" + mapper.writeValueAsString(System.currentTimeMillis())
                        + "-[");

                for (Float average : averages) {
                    payload.append(String.valueOf(average)).append(",");
                }

                payload = new StringBuilder(payload.toString().replaceAll(",$", "") + "]");

                System.out.println(payload.toString());
                MqttMessage message = new MqttMessage(payload.toString().getBytes());
                message.setQos(qos);
                client.publish(topic, message);
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
