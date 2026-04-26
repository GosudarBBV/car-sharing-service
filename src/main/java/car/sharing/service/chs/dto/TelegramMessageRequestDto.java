package car.sharing.service.chs.dto;

import jakarta.validation.constraints.NotBlank;

public record TelegramMessageRequestDto(
        @NotBlank(message = "Chat ID cannot be blank")
        String chatId,

        @NotBlank(message = "Message cannot be blank")
        String message
) {}
