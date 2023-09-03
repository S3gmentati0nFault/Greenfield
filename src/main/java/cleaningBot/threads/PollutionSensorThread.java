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

import static utilities.Variables.DEBUGGING;
import static utilities.Variables.WAKEUP_ERROR;

public class PollutionSensorThread extends Thread {
    private int district;
    private BotIdentity botIdentity;
    private MeasurementGatheringThread measurementGatheringThread;
    private boolean brokering;
    private MqttClient client;
    private String topic;

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
        topic = "greenfield/pollution/" + district;
        Logger.yellow("Publishing on topic > " + topic);

        try {
            client = new MqttClient(broker, clientID);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(false);
            connectOptions.setAutomaticReconnect(false);
            client.connect(connectOptions);

            while (brokering) {
                synchronized (this) {
                    try {
                        if (DEBUGGING) {
                            System.out.println("In attesa di un nuovo ciclo di lettura");
                        }
                        wait(15000);
                    } catch (InterruptedException e) {
                        Logger.red(WAKEUP_ERROR, e);
                    }
                    if (BotThread.getInstance().getMaintenanceThread().isDoingMaintenance()) {
                        Logger.whiteDebuggingPrint(this.getClass() + ".brokering IS WAITING");
                        wait();
                        Logger.whiteDebuggingPrint(this.getClass() + ".brokering IS NOT WAITING");
                    }
                }
                publishAverages();
            }
        } catch (MqttException e) {
            Logger.red("There was an error during the Mqtt publisher startup");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void publishAverages() {
        ObjectMapper mapper = new ObjectMapper();
        int qos = 1;

        if (DEBUGGING) {
            System.out.println(">> Mi arrubo le medie <<");
        }
        List<Float> averages = measurementGatheringThread.getAverages();
        StringBuilder payload = null;
        try {
            payload = new StringBuilder(botIdentity.getId()
                + "-" + mapper.writeValueAsString(System.currentTimeMillis())
                + "-[");
        } catch (JsonMappingException e) {
            Logger.red("There was a problem while trying to build the payload for the MQTT message", e);
        } catch (JsonGenerationException e) {
            Logger.red("There was a problem while trying to build the payload for the MQTT message", e);
        } catch (IOException e) {
            Logger.red("There was a problem while trying to build the payload for the MQTT message", e);
        }

        if(payload == null) {
            Logger.red("There was problems while trying to build the MQTT message");
            return;
        }

        for (Float average : averages) {
            payload.append(String.valueOf(average)).append(",");
        }

        payload = new StringBuilder(payload.toString().replaceAll(",$", "") + "]");

        if (DEBUGGING) {
            System.out.println(payload.toString());
        }
        MqttMessage message = new MqttMessage(payload.toString().getBytes());
        message.setQos(qos);

        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            Logger.red("There was an error while sending the MQTT message");
        }
    }

    public void closeConnection(int district) {
        Logger.yellow("Changing MQTT topic");

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }

            System.out.println("DISTRICT -> " + district);
            this.district = district;
            topic = "greenfield/pollution/" + district;
            Logger.yellow("Publishing on topic > " + topic);
        }
    }
}
