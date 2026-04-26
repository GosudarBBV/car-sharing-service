package car.sharing.service.chs.repository;

import car.sharing.service.chs.util.TestEntityFactory;
import car.sharing.service.chs.model.*;
import car.sharing.service.chs.util.BaseRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PaymentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByRental_User_Id_shouldReturnPaymentsForUser() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Payment payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        entityManager.persistAndFlush(payment);

        Page<Payment> result = paymentRepository.findAllByRental_User_Id(user.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void findByRentalIdForUpdate_shouldReturnPaymentWithLock() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Payment payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        entityManager.persistAndFlush(payment);

        Optional<Payment> result = paymentRepository.findByRentalIdForUpdate(rental.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findBySessionId_shouldReturnPayment() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Payment payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        entityManager.persistAndFlush(payment);

        Optional<Payment> result = paymentRepository.findBySessionId(payment.getSessionId());

        assertThat(result).isPresent();
    }

    @Test
    void existsByRentalIdAndStatusIn_shouldReturnTrueIfExists() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(car);
        entityManager.persistAndFlush(rental);

        Payment payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        entityManager.persistAndFlush(payment);

        boolean exists = paymentRepository.existsByRentalIdAndStatusIn(
                rental.getId(), List.of(PaymentStatus.PENDING, PaymentStatus.PAID));

        assertThat(exists).isTrue();
    }
}
