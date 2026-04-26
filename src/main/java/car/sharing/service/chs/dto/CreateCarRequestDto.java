package car.sharing.service.chs.dto;

import car.sharing.service.chs.model.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull CarType type,
        @Min(0) int inventory,
        @DecimalMin("0.0") BigDecimal dailyFee
) {
}
