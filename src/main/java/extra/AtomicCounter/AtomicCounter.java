package extra.AtomicCounter;

public class AtomicCounter {
	
	private int counter;

	public AtomicCounter() {
		counter = 0;
	}

	public AtomicCounter(int counter) {
		this.counter = counter;
	}

	public synchronized int getCounter() {
		return counter;
	}

	public synchronized void increment() {
		counter = counter + 1;
	}

	public synchronized int decrement() {
		counter = counter - 1;
		return counter;
	}

	public synchronized void add(int amount) {
		counter = counter + amount;
	}

	public synchronized void subtract(int amount) {
		counter = counter - amount;
	}
}
