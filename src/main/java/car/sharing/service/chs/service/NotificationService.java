package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.TelegramMessageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    TelegramMessageResponseDto sendNotification(TelegramMessageRequestDto dto);

    Page<TelegramMessageResponseDto> getNotificationHistory(String chatId, Pageable pageable);

    void notifyNewRental(Long rentalId);

    void notifyOverdueRentals();

    void notifyPaymentSuccess(Long paymentId);
}
