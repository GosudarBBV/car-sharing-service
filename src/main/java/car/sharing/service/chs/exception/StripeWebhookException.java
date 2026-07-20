package car.sharing.service.chs.exception;

public class StripeWebhookException extends RuntimeException {
    public StripeWebhookException(String message) {
        super(message);
    }

    public StripeWebhookException(String message, Throwable cause) {
        super(message, cause);
    }

    public StripeWebhookException(Throwable cause) {
        super(cause);
    }
}
