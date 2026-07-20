package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.TelegramMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    Page<TelegramMessage> findAllByChatId(String chatId, Pageable pageable);
}
