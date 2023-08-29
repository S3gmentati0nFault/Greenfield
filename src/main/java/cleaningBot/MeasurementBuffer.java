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
//        System.out.println("PROVO A SCRIVERE");
        if(buffer.size() < limitSize) {
            buffer.add(m);
        }
        else {
            notifyAll();
            try {
//                System.out.println("WAITING TO WRITE");
                wait();
//                System.out.println("WRITING");
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }
        }
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
//        System.out.println("PROVO A LEGGERE");
        if(buffer.size() < limitSize) {
            try{
//                System.out.println("WAITING TO READ");
                wait();
//                System.out.println("READING");
            } catch (InterruptedException e) {
                Logger.red(WAKEUP_ERROR, e);
            }
        }
        List<Measurement> measurements = new ArrayList<>(buffer);
        buffer.subList(0, (limitSize / 2)).clear();
        notifyAll();
        return measurements;
    }
}
