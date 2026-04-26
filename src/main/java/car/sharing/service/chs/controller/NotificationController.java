package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.TelegramMessageResponseDto;
import car.sharing.service.chs.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications",
        description = "Endpoints for sending and viewing notifications (Manager only)")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Send notification",
            description = "Sends a notification to a Telegram chat (Manager only)")
    public TelegramMessageResponseDto send(@RequestBody @Valid TelegramMessageRequestDto dto) {
        return notificationService.sendNotification(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get notification history",
            description = "Returns paginated notification history"
                    + " for a specific chat (Manager only)")
    public Page<TelegramMessageResponseDto> history(
            @RequestParam String chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sentAt") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        return notificationService.getNotificationHistory(chatId, pageable);
    }
}
