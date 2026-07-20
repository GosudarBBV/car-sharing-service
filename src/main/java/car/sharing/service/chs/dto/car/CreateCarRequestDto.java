package car.sharing.service.chs.dto.car;

import car.sharing.service.chs.model.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull CarType type,
        @Positive int inventory,
        @DecimalMin("0.0") BigDecimal dailyFee
) {
}
