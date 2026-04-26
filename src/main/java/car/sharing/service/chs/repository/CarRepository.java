package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Car;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdForUpdate(Long id);

    @Query("SELECT c FROM Car c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Car> findByIdAndNotDeleted(@Param("id") Long id);

    // Знайти всі невидалені машини з пагінацією
    @Query("SELECT c FROM Car c WHERE c.isDeleted = false")
    Page<Car> findAllNotDeleted(Pageable pageable);

    // Знайти тільки невидалену машину для оновлення (з блокуванням)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Car> findByIdForUpdateAndNotDeleted(@Param("id") Long id);
}
