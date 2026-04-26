package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByRental_User_Id(Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT p FROM Payment p WHERE p.rental.id = :rentalId")
    Optional<Payment> findByRentalIdForUpdate(Long rentalId);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByRentalIdAndStatusIn(Long rentalId, List<PaymentStatus> statuses);
}
