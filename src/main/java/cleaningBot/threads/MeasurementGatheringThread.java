package cleaningBot.threads;

import simulators.Measurement;
import simulators.PM10Simulator;
import cleaningBot.MeasurementBuffer;
import extra.Logger.Logger;

import java.util.ArrayList;
import java.util.List;

import static utilities.Variables.DEBUGGING;
import static utilities.Variables.WAKEUP_ERROR;

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
            if(BotThread.getInstance().getMaintenanceThread().isDoingMaintenance()) {
                buffer.setRunning(false);
                Logger.whiteDebuggingPrint(this.getClass() + " IS WAITING", DEBUGGING);
                synchronized(this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Logger.red(WAKEUP_ERROR, e);
                    }
                }
                buffer.setRunning(true);

                Logger.whiteDebuggingPrint(this.getClass() + " IS NOT WAITING", DEBUGGING);

                synchronized (buffer) {
                    buffer.notifyAll();
                }
            }
            gatherMeasurements();
        }
    }

    private void gatherMeasurements() {
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
        List<Float> returnList = new ArrayList<>(averages);
        averages.clear();
        return returnList;
    }
}
