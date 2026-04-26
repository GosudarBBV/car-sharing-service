package car.sharing.service.chs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.util.BaseRepositoryTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class RentalRepositoryTest extends BaseRepositoryTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final long DAYS_OVERDUE = 5L;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Find all rentals by user id - returns rentals for user")
    void findAllByUserId_shouldReturnRentalsForUser() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Page<Rental> result = rentalRepository.findAllByUserId(
                user.getId(), PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Find all rentals by user id where actual return date is null - returns active rentals")
    void findAllByUserIdAndActualReturnDateIsNull_shouldReturnActiveRentals() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental activeRental = TestEntityFactory.createRental(user, car);
        Rental returnedRental = TestEntityFactory.createRental(user, car);
        returnedRental.setActualReturnDate(LocalDate.now());

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(activeRental);
        entityManager.persistAndFlush(returnedRental);

        Page<Rental> result = rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                user.getId(), PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActualReturnDate()).isNull();
    }

    @Test
    @DisplayName("Find all rentals where actual return date is null - returns all active rentals")
    void findAllByActualReturnDateIsNull_shouldReturnAllActiveRentals() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental activeRental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(activeRental);

        Page<Rental> result = rentalRepository.findAllByActualReturnDateIsNull(
                PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Find rental by id for update - returns rental with lock")
    void findByIdForUpdate_shouldReturnRentalWithLock() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Optional<Rental> result = rentalRepository.findByIdForUpdate(rental.getId());

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Check if car is rented - returns true when car is currently rented")
    void existsByCarIdAndActualReturnDateIsNull_shouldReturnTrueIfCarIsRented() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        boolean exists = rentalRepository.existsByCarIdAndActualReturnDateIsNull(car.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Check if car is rented - returns false when car is not rented")
    void existsByCarIdAndActualReturnDateIsNull_shouldReturnFalseIfCarIsNotRented() {
        Car car = TestEntityFactory.createCar();
        entityManager.persistAndFlush(car);

        boolean exists = rentalRepository.existsByCarIdAndActualReturnDateIsNull(car.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Find overdue rentals - returns rentals with return date before current date")
    void findAllByReturnDateBeforeAndActualReturnDateIsNull_shouldReturnOverdueRentals() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental overdueRental = TestEntityFactory.createRental(user, car);
        overdueRental.setReturnDate(LocalDate.now().minusDays(DAYS_OVERDUE));

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(overdueRental);

        List<Rental> result = rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(
                LocalDate.now());

        assertThat(result).isNotEmpty();
    }
}
