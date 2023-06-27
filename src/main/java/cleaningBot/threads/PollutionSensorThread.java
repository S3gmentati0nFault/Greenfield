package cleaningBot.threads;

import extra.Logger.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

public class PollutionSensorThread extends Thread {
    private int district;
    private MeasurementGatheringThread measurementGatheringThread;

    public PollutionSensorThread() {
        district = 0;
    }

    public PollutionSensorThread(int district) {
        this.district = district;
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
        int qos = 1;

        try{
            client = new MqttClient(broker, clientID);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(false);
            client.connect(connectOptions);
            while(true) {
                try{
                    wait(15000);
                } catch (InterruptedException e) {
                    Logger.red("There was an error during the wakeup procedure");
                }

                Logger.yellow("Preparing to send data");
                List<Float> averages = measurementGatheringThread.getAverages();
                String payload = "";

                for (Float average : averages) {
                    payload = payload + String.valueOf(average) + "-";
                }

                System.out.println(payload);

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
            }
        } catch (MqttException e) {
            Logger.red("There was an error during the Mqtt publisher startup");
            e.printStackTrace();
        }
    }
}
