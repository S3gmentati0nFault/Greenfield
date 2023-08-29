package extra.AtomicFlag;

public class AtomicFlag {
    private boolean flag;

    public AtomicFlag(boolean flag) {
        this.flag = flag;
    }

    public synchronized boolean isFlag() {
        return flag;
    }

    public synchronized void setFlag(boolean flag) {
        this.flag = flag;
    }
}
