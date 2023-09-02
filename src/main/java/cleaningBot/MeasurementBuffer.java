package cleaningBot;

import simulators.Measurement;
import extra.Logger.Logger;

import java.util.ArrayList;
import java.util.List;

import static utilities.Variables.WAKEUP_ERROR;

public class MeasurementBuffer implements simulators.Buffer {
    private List<Measurement> buffer;
    private int limitSize;
    private boolean isRunning;


    public MeasurementBuffer() {
        buffer = new ArrayList<>();
        limitSize = 8;
        isRunning = true;
    }

    @Override
    public synchronized void addMeasurement(Measurement m) {
        if(!isRunning) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if(buffer.size() < limitSize) {
            buffer.add(m);
        }
        else {
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }
        }
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        if(!isRunning) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if(buffer.size() < limitSize) {
            try{
                wait();
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }
        }
        List<Measurement> measurements = new ArrayList<>(buffer);
        buffer.subList(0, (limitSize / 2)).clear();
        notifyAll();
        return measurements;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
