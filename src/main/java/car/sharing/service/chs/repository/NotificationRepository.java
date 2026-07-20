package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);

    Page<Notification> findAllBySentFalse(Pageable pageable);
}
