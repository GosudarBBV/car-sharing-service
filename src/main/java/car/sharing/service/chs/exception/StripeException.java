package car.sharing.service.chs.exception;

public class StripeException extends RuntimeException {
    public StripeException(String message, Throwable cause) {
        super(message, cause);
    }
}
