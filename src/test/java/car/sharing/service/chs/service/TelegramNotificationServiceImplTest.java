package car.sharing.service.chs.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.service.chs.dto.notication.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.notication.TelegramMessageResponseDto;
import car.sharing.service.chs.exception.TelegramSendFailedException;
import car.sharing.service.chs.mapper.TelegramMessageMapper;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import car.sharing.service.chs.model.PaymentType;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.TelegramMessage;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.PaymentRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.repository.TelegramMessageRepository;
import car.sharing.service.chs.util.TestEntityFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceImplTest {
    private static final String ADMIN_CHAT_ID = "8631775085";
    private static final String TEST_MESSAGE = "Test notification message";
    private static final long TEST_RENTAL_ID = 1L;
    private static final long TEST_PAYMENT_ID = 1L;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Mock
    private TelegramBot bot;

    @Mock
    private TelegramMessageRepository messageRepository;

    @Mock
    private TelegramMessageMapper mapper;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private TelegramNotificationServiceImpl notificationService;

    private TelegramMessageRequestDto requestDto;
    private TelegramMessage message;
    private TelegramMessageResponseDto responseDto;
    private Rental rental;
    private Payment payment;

    @BeforeEach
    void setUp() throws Exception {
        Field adminChatIdField = TelegramNotificationServiceImpl.class.getDeclaredField("adminChatId");
        adminChatIdField.setAccessible(true);
        adminChatIdField.set(notificationService, ADMIN_CHAT_ID);

        requestDto = TestEntityFactory.createTelegramMessageRequest(ADMIN_CHAT_ID, TEST_MESSAGE);

        message = TestEntityFactory.createTelegramMessage(ADMIN_CHAT_ID, TEST_MESSAGE);
        message.setId(1L);

        responseDto = new TelegramMessageResponseDto(1L, ADMIN_CHAT_ID, TEST_MESSAGE, message.getSentAt());

        User user = TestEntityFactory.createUser();
        Car car = TestEntityFactory.createCar();
        rental = TestEntityFactory.createRental(user, car);
        rental.setId(TEST_RENTAL_ID);

        payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        payment.setId(TEST_PAYMENT_ID);
        payment.setAmount(BigDecimal.valueOf(120.00));
    }

    @Test
    @DisplayName("Send notification - sends message successfully and returns response")
    void sendNotification_Success_ReturnsResponse() throws TelegramApiException {
        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(message);
        when(mapper.toDto(message)).thenReturn(responseDto);

        TelegramMessageResponseDto result = notificationService.sendNotification(requestDto);

        assertNotNull(result);
        assertEquals(ADMIN_CHAT_ID, result.chatId());
        assertEquals(TEST_MESSAGE, result.message());

        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository).save(any(TelegramMessage.class));
        verify(mapper).toDto(message);
    }

    @Test
    @DisplayName("Send notification - throws exception when Telegram API fails")
    void sendNotification_TelegramApiFailed_ThrowsException() throws TelegramApiException {
        when(bot.execute(any(SendMessage.class))).thenThrow(new TelegramApiException("API error"));

        assertThrows(TelegramSendFailedException.class,
                () -> notificationService.sendNotification(requestDto));

        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository, never()).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Get notification history - returns page of messages")
    void getNotificationHistory_ReturnsPageOfMessages() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<TelegramMessage> messagePage = new PageImpl<>(List.of(message));
        Page<TelegramMessageResponseDto> expectedPage = new PageImpl<>(List.of(responseDto));

        when(messageRepository.findAllByChatId(ADMIN_CHAT_ID, pageable)).thenReturn(messagePage);
        when(mapper.toDto(message)).thenReturn(responseDto);

        Page<TelegramMessageResponseDto> result = notificationService.getNotificationHistory(ADMIN_CHAT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(responseDto, result.getContent().get(0));

        verify(messageRepository).findAllByChatId(ADMIN_CHAT_ID, pageable);
        verify(mapper).toDto(message);
    }

    @Test
    @DisplayName("Get notification history - returns empty page when no messages")
    void getNotificationHistory_NoMessages_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<TelegramMessage> emptyPage = new PageImpl<>(Collections.emptyList());

        when(messageRepository.findAllByChatId(ADMIN_CHAT_ID, pageable)).thenReturn(emptyPage);

        Page<TelegramMessageResponseDto> result = notificationService.getNotificationHistory(ADMIN_CHAT_ID, pageable);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());

        verify(messageRepository).findAllByChatId(ADMIN_CHAT_ID, pageable);
        verify(mapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Notify new rental - sends notification successfully")
    void notifyNewRental_Success() throws TelegramApiException {
        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(message);
        when(mapper.toDto(message)).thenReturn(responseDto);

        notificationService.notifyNewRental(TEST_RENTAL_ID);

        verify(rentalRepository).findById(TEST_RENTAL_ID);
        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Notify new rental - throws exception when rental not found")
    void notifyNewRental_RentalNotFound_ThrowsException() throws TelegramApiException {
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> notificationService.notifyNewRental(TEST_RENTAL_ID));

        verify(rentalRepository).findById(TEST_RENTAL_ID);
        verify(bot, never()).execute(any(SendMessage.class));
        verify(messageRepository, never()).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Notify overdue rentals - sends notifications for overdue rentals")
    void notifyOverdueRentals_SendsNotifications() throws TelegramApiException {
        List<Rental> overdueRentals = List.of(rental);
        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now()))
                .thenReturn(overdueRentals);
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(message);
        when(mapper.toDto(message)).thenReturn(responseDto);

        notificationService.notifyOverdueRentals();

        verify(rentalRepository).findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now());
        verify(bot, times(1)).execute(any(SendMessage.class));
        verify(messageRepository, times(1)).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Notify overdue rentals - sends no overdue message when no overdue rentals")
    void notifyOverdueRentals_NoOverdueRentals_SendsNoOverdueMessage() throws TelegramApiException {
        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now()))
                .thenReturn(Collections.emptyList());
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(message);
        when(mapper.toDto(message)).thenReturn(responseDto);

        notificationService.notifyOverdueRentals();

        verify(rentalRepository).findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now());
        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Notify payment success - sends notification successfully")
    void notifyPaymentSuccess_Success() throws TelegramApiException {
        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(message);
        when(mapper.toDto(message)).thenReturn(responseDto);

        notificationService.notifyPaymentSuccess(TEST_PAYMENT_ID);

        verify(paymentRepository).findById(TEST_PAYMENT_ID);
        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Notify payment success - throws exception when payment not found")
    void notifyPaymentSuccess_PaymentNotFound_ThrowsException() throws TelegramApiException {
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> notificationService.notifyPaymentSuccess(TEST_PAYMENT_ID));

        verify(paymentRepository).findById(TEST_PAYMENT_ID);
        verify(bot, never()).execute(any(SendMessage.class));
        verify(messageRepository, never()).save(any(TelegramMessage.class));
    }

    @Test
    @DisplayName("Send notification with empty message - still sends")
    void sendNotification_EmptyMessage_SendsSuccessfully() throws TelegramApiException {
        TelegramMessageRequestDto emptyRequest = TestEntityFactory.createTelegramMessageRequest(ADMIN_CHAT_ID, "");
        TelegramMessage emptyMessage = TestEntityFactory.createTelegramMessage(ADMIN_CHAT_ID, "");
        emptyMessage.setId(2L);

        TelegramMessageResponseDto emptyResponse = new TelegramMessageResponseDto(
                2L, ADMIN_CHAT_ID, "", emptyMessage.getSentAt());

        Message sentMessage = new Message();
        sentMessage.setMessageId(123);

        when(bot.execute(any(SendMessage.class))).thenReturn(sentMessage);
        when(messageRepository.save(any(TelegramMessage.class))).thenReturn(emptyMessage);
        when(mapper.toDto(emptyMessage)).thenReturn(emptyResponse);

        TelegramMessageResponseDto result = notificationService.sendNotification(emptyRequest);

        assertNotNull(result);
        assertEquals("", result.message());

        verify(bot).execute(any(SendMessage.class));
        verify(messageRepository).save(any(TelegramMessage.class));
    }
}
