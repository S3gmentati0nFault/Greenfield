package exceptions;

public class AlreadyOnMaintenanceException extends Exception{
    public AlreadyOnMaintenanceException() {
        super();
    }

    public AlreadyOnMaintenanceException(String message) {
        super(message);
    }

    public AlreadyOnMaintenanceException(Throwable cause) {
        super(cause);
    }
}
