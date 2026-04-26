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
    @DisplayName("Find car by id for update - returns car when exists")
    void findByIdForUpdate_existingCar_returnsCar() {
        Car car = TestEntityFactory.createCar();
        entityManager.persistAndFlush(car);

        Car result = carRepository.findByIdForUpdate(car.getId())
                .orElseThrow();

        assertThat(result.getId()).isEqualTo(car.getId());
        assertThat(result.getBrand()).isEqualTo("Tesla");
    }

    @Test
    @DisplayName("Find car by id for update - returns empty when car does not exist")
    void findByIdForUpdate_nonExistingCar_returnsEmpty() {
        Optional<Car> result = carRepository.findByIdForUpdate(NON_EXISTENT_CAR_ID);

        assertThat(result).isEmpty();
    }
}
