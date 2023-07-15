import extra.AtomicCounter.AtomicCounter;
import extra.CustomRandom.CustomRandom;
import extra.Logger.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Test {

    private static AtomicCounter counter;
    private static final String file = "test.txt";

    public static class TestThread extends Thread {
        private int wait;
        private BufferedWriter bw;

        public TestThread(int randomWait) {
            wait = randomWait;
        }

        @Override
        public void run() {
//            System.out.println("Thread " + this.getId() + " will wait for " + wait + " seconds");
            try {
                Thread.sleep(wait * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                bw = new BufferedWriter(new FileWriter(file, true));
            } catch (Exception e) {
                System.out.println("An error occurred while writing to the file: " + e.getMessage());
            }
            for(int i = 0; i < 5; ++i) {
                try {
                    addOne(this.getId(), bw);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        counter = new AtomicCounter();
        TestThread threads[] = new TestThread[1000];

        for (TestThread thread : threads) {
            int randomWait = CustomRandom.getInstance().rnInt(20);
            thread = new TestThread(randomWait);
            thread.start();
        }
    }

    public static void addOne(long id, BufferedWriter bw) throws IOException {
        counter.increment();
//        if(counter.getCounter() % 50 == 0) {
//            Logger.blue("We are at " + counter.getCounter() + " id: " + id);
//        }

        if(counter.getCounter() == 5000) {
            Logger.red("We hit the limit");
        }
        bw.write(counter.getCounter());
        bw.newLine();
    }
}
