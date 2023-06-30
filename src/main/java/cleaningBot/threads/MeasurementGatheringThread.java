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
        gatherMeasurements();
    }

    private void gatherMeasurements() {
        while(true) {
            List<Measurement> measurements = buffer.readAllAndClean();
            float avg = 0;
            for (Measurement measurement : measurements) {
                avg += measurement.getValue();
            }
            avg = avg / 8f;
            averages.add(avg);
        }
    }

    public List<Float> getAverages() {
        return averages;
    }

    public synchronized void clear() {
        averages.clear();
    }
}
