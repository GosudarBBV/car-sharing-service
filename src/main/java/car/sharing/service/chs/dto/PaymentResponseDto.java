package car.sharing.service.chs.dto;

import car.sharing.service.chs.model.PaymentStatus;
import car.sharing.service.chs.model.PaymentType;
import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        PaymentStatus status,
        PaymentType type,
        Long rentalId,
        BigDecimal amount,
        String sessionId,
        String sessionUrl
) {}
