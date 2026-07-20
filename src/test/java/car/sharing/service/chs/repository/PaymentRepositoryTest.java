package car.sharing.service.chs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import car.sharing.service.chs.model.PaymentType;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.util.BaseRepositoryTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class PaymentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByRental_User_Id_ShouldReturnPaymentsForUser() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persist(user);
        entityManager.persist(car);
        entityManager.persist(rental);

        Payment payment = TestEntityFactory.createPayment(
                rental,
                PaymentStatus.PENDING,
                PaymentType.PAYMENT
        );
        entityManager.persistAndFlush(payment);

        Page<Payment> result = paymentRepository.findAllByRental_User_Id(
                user.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent().getFirst().getRental().getId())
                .isEqualTo(rental.getId());
    }

    @Test
    void findBySessionId_ShouldReturnPayment() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persist(user);
        entityManager.persist(car);
        entityManager.persist(rental);

        Payment payment = TestEntityFactory.createPayment(
                rental,
                PaymentStatus.PENDING,
                PaymentType.PAYMENT
        );
        entityManager.persistAndFlush(payment);

        Optional<Payment> result =
                paymentRepository.findBySessionId(payment.getSessionId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(payment.getId());
        assertThat(result.get().getSessionId())
                .isEqualTo(payment.getSessionId());
    }

    @Test
    void findBySessionId_ShouldReturnEmpty_WhenPaymentDoesNotExist() {
        Optional<Payment> result =
                paymentRepository.findBySessionId("unknown_session");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByRentalIdAndStatusIn_ShouldReturnTrue_WhenPaymentExists() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persist(user);
        entityManager.persist(car);
        entityManager.persist(rental);

        Payment payment = TestEntityFactory.createPayment(
                rental,
                PaymentStatus.PENDING,
                PaymentType.PAYMENT
        );
        entityManager.persistAndFlush(payment);

        boolean exists = paymentRepository.existsByRentalIdAndStatusIn(
                rental.getId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByRentalIdAndStatusIn_ShouldReturnFalse_WhenStatusDoesNotMatch() {
        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        Rental rental = TestEntityFactory.createRental(user, car);

        entityManager.persist(user);
        entityManager.persist(car);
        entityManager.persist(rental);

        Payment payment = TestEntityFactory.createPayment(
                rental,
                PaymentStatus.CANCELED,
                PaymentType.PAYMENT
        );
        entityManager.persistAndFlush(payment);

        boolean exists = paymentRepository.existsByRentalIdAndStatusIn(
                rental.getId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        );

        assertThat(exists).isFalse();
    }
}
