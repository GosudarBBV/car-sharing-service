package car.sharing.service.chs.exception;

public class TelegramSendFailedException extends RuntimeException {
    public TelegramSendFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
