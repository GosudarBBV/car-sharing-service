package car.sharing.service.chs.dto.notication;

import java.time.LocalDateTime;

public record TelegramMessageResponseDto(
        Long id,
        String chatId,
        String message,
        LocalDateTime sentAt
) {}
