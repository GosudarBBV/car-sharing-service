package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Rental;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findAllByUserId(Long userId, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNull(Long userId, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNotNull(Long userId, Pageable pageable);

    Page<Rental> findAllByActualReturnDateIsNull(Pageable pageable);

    Page<Rental> findAllByActualReturnDateIsNotNull(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT r FROM Rental r WHERE r.id = :id")
    Optional<Rental> findByIdForUpdate(Long id);

    boolean existsByCarIdAndActualReturnDateIsNull(Long carId);

    List<Rental> findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate date);
}
