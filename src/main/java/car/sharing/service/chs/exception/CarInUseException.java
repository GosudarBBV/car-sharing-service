package car.sharing.service.chs.exception;

public class CarInUseException extends RuntimeException {
    public CarInUseException(Long id) {
        super("Car with id " + id + " is currently rented");
    }
}
