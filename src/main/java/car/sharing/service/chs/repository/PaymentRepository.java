package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByRental_User_Id(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByRentalIdAndStatusIn(Long rentalId, List<PaymentStatus> statuses);
}
