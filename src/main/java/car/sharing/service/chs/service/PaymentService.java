package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.PaymentRequestDto;
import car.sharing.service.chs.dto.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<PaymentResponseDto> getPayments(String userEmail,
                                         Pageable pageable);

    PaymentResponseDto createPaymentSession(PaymentRequestDto dto,
                                            String userEmail);

    void handleCancel(String sessionId);

    void handleStripeWebhook(String payload, String signature);
}
