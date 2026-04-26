package car.sharing.service.chs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.service.chs.dto.PaymentRequestDto;
import car.sharing.service.chs.dto.PaymentResponseDto;
import car.sharing.service.chs.exception.*;
import car.sharing.service.chs.mapper.PaymentMapper;
import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import car.sharing.service.chs.model.PaymentType;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.PaymentRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.repository.UserRepository;
import car.sharing.service.chs.service.stripe.StripePaymentProvider;
import car.sharing.service.chs.util.TestEntityFactory;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    private static final String USER_EMAIL = "test@example.com";
    private static final String SESSION_ID = "cs_test_123";
    private static final String SESSION_URL = "https://checkout.stripe.com/session/123";
    private static final String SUCCESS_URL = "http://localhost:8080/payments/success";
    private static final String CANCEL_URL = "http://localhost:8080/payments/cancel";
    private static final String WEBHOOK_PAYLOAD = "{}";
    private static final String WEBHOOK_SIGNATURE = "signature";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("120.00");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private StripePaymentProvider stripeProvider;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Session stripeSession;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private Rental rental;
    private Payment payment;
    private PaymentRequestDto requestDto;
    private PaymentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = TestEntityFactory.createUser();
        user.setEmail(USER_EMAIL);
        user.setId(1L);

        rental = TestEntityFactory.createRental(user, TestEntityFactory.createCar());
        rental.setId(1L);

        payment = TestEntityFactory.createPayment(rental, PaymentStatus.PENDING, PaymentType.PAYMENT);
        payment.setId(1L);
        payment.setSessionId(SESSION_ID);
        payment.setSessionUrl(SESSION_URL);
        payment.setAmount(VALID_AMOUNT);

        requestDto = new PaymentRequestDto(rental.getId(), PaymentType.PAYMENT, VALID_AMOUNT);

        responseDto = new PaymentResponseDto(
                payment.getId(),
                PaymentStatus.PENDING,
                PaymentType.PAYMENT,
                rental.getId(),
                VALID_AMOUNT,
                SESSION_ID,
                SESSION_URL
        );

        ReflectionTestUtils.setField(paymentService, "successUrl", SUCCESS_URL);
        ReflectionTestUtils.setField(paymentService, "cancelUrl", CANCEL_URL);
    }

    @Test
    @DisplayName("Create payment session - throws exception when rental not found")
    void createPaymentSession_RentalNotFound_ThrowsException() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.empty());

        assertThrows(RentalNotFoundException.class,
                () -> paymentService.createPaymentSession(requestDto, USER_EMAIL));
    }

    @Test
    @DisplayName("Create payment session - throws exception when rental belongs to another user")
    void createPaymentSession_AccessDenied_ThrowsException() {
        User anotherUser = TestEntityFactory.createUser();
        anotherUser.setId(999L);
        rental.setUser(anotherUser);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(PaymentAccessDeniedException.class,
                () -> paymentService.createPaymentSession(requestDto, USER_EMAIL));
    }

    @Test
    @DisplayName("Create payment session - throws exception when payment already exists")
    void createPaymentSession_DuplicatePayment_ThrowsException() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(paymentRepository.findByRentalIdForUpdate(rental.getId())).thenReturn(Optional.of(payment));

        assertThrows(DuplicatePaymentException.class,
                () -> paymentService.createPaymentSession(requestDto, USER_EMAIL));
    }

    @Test
    @DisplayName("Create payment session - creates payment successfully")
    void createPaymentSession_Success() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(paymentRepository.findByRentalIdForUpdate(rental.getId())).thenReturn(Optional.empty());
        when(stripeProvider.createSession(
                eq(rental.getId()), eq(user.getId()), eq(VALID_AMOUNT),
                eq(PaymentType.PAYMENT.name()), eq("Rental #" + rental.getId()),
                eq(SUCCESS_URL), eq(CANCEL_URL)
        )).thenReturn(stripeSession);
        when(stripeSession.getId()).thenReturn(SESSION_ID);
        when(stripeSession.getUrl()).thenReturn(SESSION_URL);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPaymentSession(requestDto, USER_EMAIL);

        assertNotNull(result);
        assertEquals(responseDto, result);

        Payment saved = captor.getValue();
        assertEquals(rental.getId(), saved.getRental().getId());
        assertEquals(VALID_AMOUNT, saved.getAmount());
        assertEquals(SESSION_ID, saved.getSessionId());
        assertEquals(SESSION_URL, saved.getSessionUrl());

        verify(stripeProvider).createSession(
                eq(rental.getId()), eq(user.getId()), eq(VALID_AMOUNT),
                eq(PaymentType.PAYMENT.name()), eq("Rental #" + rental.getId()),
                eq(SUCCESS_URL), eq(CANCEL_URL)
        );
        verify(paymentRepository).save(captor.capture());
        verify(paymentMapper).toDto(any(Payment.class));
    }

    @Test
    @DisplayName("Create payment session - throws exception when amount is invalid")
    void createPaymentSession_InvalidAmount_ThrowsException() {
        PaymentRequestDto invalidRequest = new PaymentRequestDto(rental.getId(), PaymentType.PAYMENT, BigDecimal.ZERO);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(InvalidPaymentAmountException.class,
                () -> paymentService.createPaymentSession(invalidRequest, USER_EMAIL));
    }

    @Test
    @DisplayName("Get payments - returns page of payments for user")
    void getPayments_ReturnsPageOfPayments() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Payment> paymentPage = new PageImpl<>(java.util.List.of(payment));
        Page<PaymentResponseDto> expectedPage = new PageImpl<>(java.util.List.of(responseDto));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(paymentRepository.findAllByRental_User_Id(user.getId(), pageable)).thenReturn(paymentPage);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        Page<PaymentResponseDto> result = paymentService.getPayments(USER_EMAIL, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(responseDto, result.getContent().get(0));
    }

    @Test
    @DisplayName("Get payments - throws exception when user not found")
    void getPayments_UserNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> paymentService.getPayments(USER_EMAIL, pageable));
    }

    @Test
    @DisplayName("Handle cancel - cancels payment successfully")
    void handleCancel_Success() {
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.handleCancel(SESSION_ID);

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());

        verify(paymentRepository).findBySessionId(SESSION_ID);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Handle cancel - throws exception when payment not found")
    void handleCancel_PaymentNotFound_ThrowsException() {
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.handleCancel(SESSION_ID));
    }

    @Test
    @DisplayName("Handle cancel - throws exception when payment already paid")
    void handleCancel_PaymentAlreadyPaid_ThrowsException() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> paymentService.handleCancel(SESSION_ID));
    }

    @Test
    @DisplayName("Handle Stripe webhook - processes webhook successfully")
    void handleStripeWebhook_Success() throws Exception {
        when(stripeProvider.validateAndGetSessionFromWebhook(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE))
                .thenReturn(stripeSession);
        when(stripeSession.getId()).thenReturn(SESSION_ID);
        when(stripeSession.getAmountTotal()).thenReturn(12000L);
        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.handleStripeWebhook(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE);

        assertEquals(PaymentStatus.PAID, payment.getStatus());

        verify(stripeProvider).validateAndGetSessionFromWebhook(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE);
        verify(paymentRepository).findBySessionId(SESSION_ID);
        verify(paymentRepository).save(payment);
    }
}
