package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.notication.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.notication.TelegramMessageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    TelegramMessageResponseDto sendNotification(TelegramMessageRequestDto dto);

    Page<TelegramMessageResponseDto> getNotificationHistory(String chatId, Pageable pageable);

    void notifyNewRental(Long rentalId);

    void notifyOverdueRentals();

    void notifyPaymentSuccess(Long paymentId);
}
