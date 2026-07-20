package car.sharing.service.chs.dto.payment;

import car.sharing.service.chs.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequestDto(
        @NotNull Long rentalId,
        @NotNull PaymentType type,
        @NotNull @Positive BigDecimal amount
) {}
