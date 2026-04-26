package car.sharing.service.chs.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(Long rentalId) {
        super("Payment already exists for rental id: " + rentalId);
    }
}
