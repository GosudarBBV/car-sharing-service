package car.sharing.service.chs.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import car.sharing.service.chs.TestEntityFactory;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.util.BaseRepositoryTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

class CarRepositoryTest extends BaseRepositoryTest {
    private static final long NON_EXISTENT_CAR_ID = 999L;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Find not deleted car by id - returns car when exists")
    void findByIdAndNotDeleted_existingCar_returnsCar() {
        Car car = TestEntityFactory.createCar();
        entityManager.persistAndFlush(car);

        Optional<Car> result = carRepository.findByIdAndNotDeleted(car.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(car.getId());
        assertThat(result.get().getBrand()).isEqualTo("Tesla");
    }

    @Test
    @DisplayName("Find not deleted car by id - returns empty when car does not exist")
    void findByIdAndNotDeleted_nonExistingCar_returnsEmpty() {
        Optional<Car> result = carRepository.findByIdAndNotDeleted(NON_EXISTENT_CAR_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find not deleted car by id - returns empty when car is deleted")
    void findByIdAndNotDeleted_deletedCar_returnsEmpty() {
        Car car = TestEntityFactory.createCar();
        car.setDeleted(true);
        entityManager.persistAndFlush(car);

        Optional<Car> result = carRepository.findByIdAndNotDeleted(car.getId());

        assertThat(result).isEmpty();
    }
}
