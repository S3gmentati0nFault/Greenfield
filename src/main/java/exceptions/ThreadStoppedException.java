package exceptions;

public class ThreadStoppedException extends Exception {
    public ThreadStoppedException(){
        super();
    }

    public ThreadStoppedException(String message){
        super(message);
    }
}
