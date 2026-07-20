package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.payment.PaymentRequestDto;
import car.sharing.service.chs.dto.payment.PaymentResponseDto;
import car.sharing.service.chs.exception.DuplicatePaymentException;
import car.sharing.service.chs.exception.InvalidPaymentAmountException;
import car.sharing.service.chs.exception.PaymentAccessDeniedException;
import car.sharing.service.chs.exception.StripeWebhookException;
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
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final StripePaymentProvider stripeProvider;
    private final NotificationService notificationService;

    @Value("${app.payment.success-url:http://localhost:8080/payments/success}")
    private String successUrl;

    @Value("${app.payment.cancel-url:http://localhost:8080/payments/cancel}")
    private String cancelUrl;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPayments(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return paymentRepository.findAllByRental_User_Id(user.getId(), pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    public PaymentResponseDto createPaymentSession(PaymentRequestDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Rental rental = validateRental(dto.rentalId(), user.getId());
        validateAmount(dto.amount());

        if (paymentRepository.existsByRentalIdAndStatusIn(
                rental.getId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        )) {
            throw new DuplicatePaymentException(rental.getId());
        }

        String productName = dto.type() == PaymentType.PAYMENT
                ? "Rental #" + rental.getId()
                : "Fine #" + rental.getId();

        Session session = stripeProvider.createSession(
                rental.getId(),
                rental.getUser().getId(),
                dto.amount(),
                dto.type().name(),
                productName,
                successUrl,
                cancelUrl
        );

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setAmount(dto.amount());
        payment.setType(dto.type());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());

        paymentRepository.save(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    public void handleCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found with session id: " + sessionId
                ));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot cancel paid payment");
        }

        payment.setStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);
    }

    @Override
    public void handleStripeWebhook(String payload, String signature) {
        try {
            Session session = stripeProvider.validateAndGetSessionFromWebhook(payload, signature);

            Payment payment = paymentRepository.findBySessionId(session.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Payment not found with session id: " + session.getId()
                    ));

            BigDecimal stripeAmount = BigDecimal.valueOf(session.getAmountTotal())
                    .divide(BigDecimal.valueOf(100));

            if (payment.getAmount().compareTo(stripeAmount) != 0) {
                throw new StripeWebhookException("Amount mismatch");
            }

            if (payment.getStatus() == PaymentStatus.PAID) {
                return;
            }

            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            notificationService.notifyPaymentSuccess(payment.getId());

        } catch (Exception e) {
            throw new StripeWebhookException("Webhook failed: " + e.getMessage(), e);
        }
    }

    private Rental validateRental(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Rental not found with id: " + rentalId));

        if (!rental.getUser().getId().equals(userId)) {
            throw new PaymentAccessDeniedException();
        }

        return rental;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException();
        }
    }
}
