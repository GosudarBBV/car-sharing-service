package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.notication.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.notication.TelegramMessageResponseDto;
import car.sharing.service.chs.exception.TelegramSendFailedException;
import car.sharing.service.chs.mapper.TelegramMessageMapper;
import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.TelegramMessage;
import car.sharing.service.chs.repository.PaymentRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.repository.TelegramMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements NotificationService {
    private final TelegramBot bot;
    private final TelegramMessageRepository messageRepository;
    private final TelegramMessageMapper mapper;
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;

    @Value("${telegram.admin.chat-id}")
    private String adminChatId;

    @Override
    @Transactional
    public TelegramMessageResponseDto sendNotification(TelegramMessageRequestDto dto) {
        log.info("Sending notification to chatId: {}", dto.chatId());

        SendMessage message = new SendMessage(dto.chatId(), dto.message());
        try {
            bot.execute(message);
            log.debug("Message sent successfully to chatId: {}", dto.chatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}", dto.chatId(), e);
            throw new TelegramSendFailedException("Telegram send failed: " + e.getMessage(), e);
        }

        TelegramMessage entity = new TelegramMessage();
        entity.setChatId(dto.chatId());
        entity.setMessage(dto.message());
        entity.setSentAt(LocalDateTime.now());

        TelegramMessage saved = messageRepository.save(entity);
        log.info("Notification saved with id: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TelegramMessageResponseDto> getNotificationHistory(String chatId,
                                                                   Pageable pageable) {
        log.debug("Getting notification history for chatId: {}", chatId);
        return messageRepository.findAllByChatId(chatId, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public void notifyNewRental(Long rentalId) {
        log.info("Notifying about new rental: {}", rentalId);

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> {
                    log.error("Rental not found with id: {}", rentalId);
                    return new EntityNotFoundException("Rental not found with id: "
                            + rentalId);
                });

        String msg = String.format("🚗 New rental #%d created", rental.getId());
        sendNotification(new TelegramMessageRequestDto(adminChatId, msg));
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyOverdueRentals() {
        log.info("Checking for overdue rentals at 9:00 AM");

        List<Rental> overdue = rentalRepository
                .findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now());

        if (overdue.isEmpty()) {
            log.info("No overdue rentals found");
            sendNotification(new TelegramMessageRequestDto(adminChatId, "✅ No overdue rentals"));
            return;
        }

        log.info("Found {} overdue rentals", overdue.size());
        for (Rental rental : overdue) {
            String msg = String.format("⚠️ Overdue rental #%d", rental.getId());
            sendNotification(new TelegramMessageRequestDto(adminChatId, msg));
        }
    }

    @Override
    @Transactional
    public void notifyPaymentSuccess(Long paymentId) {
        log.info("Notifying about successful payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with id: {}", paymentId);
                    return new EntityNotFoundException(
                            "Payment not found with session id: " + paymentId
                    );
                });

        String msg = String.format("💰 Payment success $%.2f", payment.getAmount());
        sendNotification(new TelegramMessageRequestDto(adminChatId, msg));
    }
}
