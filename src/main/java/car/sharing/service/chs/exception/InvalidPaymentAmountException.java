package car.sharing.service.chs.exception;

public class InvalidPaymentAmountException extends RuntimeException {
    public InvalidPaymentAmountException() {
        super("Amount must be greater than zero");
    }
}
