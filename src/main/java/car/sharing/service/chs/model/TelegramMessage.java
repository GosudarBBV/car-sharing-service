package car.sharing.service.chs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "telegram_messages",
        indexes = {
                @Index(name = "idx_telegram_chat_id", columnList = "chat_id"),
                @Index(name = "idx_telegram_sent_at", columnList = "sent_at")
        }
)
@Getter
@Setter
public class TelegramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
