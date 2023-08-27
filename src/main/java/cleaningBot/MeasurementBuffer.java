package cleaningBot;

import simulators.Measurement;
import extra.Logger.Logger;

import java.util.ArrayList;
import java.util.List;

import static utilities.Variables.WAKEUP_ERROR;

public class MeasurementBuffer implements simulators.Buffer {
    private List<Measurement> buffer;
    private int limitSize;


    public MeasurementBuffer() {
        buffer = new ArrayList<>();
        limitSize = 8;
    }

    @Override
    public synchronized void addMeasurement(Measurement m) {
        buffer.add(m);
        if(buffer.size() == limitSize) {
            notifyAll();
        }
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        if(buffer.size() < limitSize) {
            try{
                wait();
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e.getCause());
            }
        }
        List<Measurement> measurements = new ArrayList<>(buffer);
        buffer.subList(limitSize / 2, limitSize).clear();
        return measurements;
    }
}
