package car.sharing.service.chs.exception;

public class PaymentAccessDeniedException extends RuntimeException {
    public PaymentAccessDeniedException() {
        super("Cannot pay for another user's rental");
    }
}
