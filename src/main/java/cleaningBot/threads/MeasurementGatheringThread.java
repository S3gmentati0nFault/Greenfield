package cleaningBot.threads;

import simulators.Measurement;
import simulators.PM10Simulator;
import cleaningBot.MeasurementBuffer;
import extra.Logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class MeasurementGatheringThread extends Thread {
    List<Float> averages;
    MeasurementBuffer buffer;

    public MeasurementGatheringThread() {
        averages = new ArrayList<>();
    }

    @Override
    public void run() {
        buffer = new MeasurementBuffer();
        PM10Simulator simulatorThread = new PM10Simulator(buffer);

        Logger.yellow("Starting the simulation thread");
        simulatorThread.start();
        readingProcess();
    }

    public void readingProcess() {
        while(true) {
            gatherMeasurements();
//            System.out.println("CICLANDO...");
        }
    }

    private void gatherMeasurements() {
//        System.out.println("CONTATTO IL SENSORE");
            List<Measurement> measurements = buffer.readAllAndClean();
            float avg = 0;
            for (Measurement measurement : measurements) {
                avg += measurement.getValue();
            }
            avg = avg / 8f;
            synchronized(this) {
                averages.add(avg);
            }
    }

    public synchronized List<Float> getAverages() {
        System.out.println("PASSO LE MEDIE AL MIO SUPERIORE");
        List<Float> returnList = new ArrayList<>(averages);
        averages.clear();
        return returnList;
    }
}
