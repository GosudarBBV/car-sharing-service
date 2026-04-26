package car.sharing.service.chs.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String sessionId) {
        super("Payment not found for sessionId: " + sessionId);
    }

    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found for id: " + paymentId);
    }
}
