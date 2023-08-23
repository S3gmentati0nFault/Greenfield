package exceptions;

public class BotAlreadyExistsException extends Exception {
    public BotAlreadyExistsException() {
        super();
    }

    public BotAlreadyExistsException(String message) {
        super(message);
    }

    public BotAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
