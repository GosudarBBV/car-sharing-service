package car.sharing.service.chs.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(Long id) {
        super("Rental not found with id: " + id);
    }
}
