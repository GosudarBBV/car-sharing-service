package car.sharing.service.chs.dto;

import car.sharing.service.chs.model.CarType;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String brand,
        String model,
        CarType type,
        int inventory,
        BigDecimal dailyFee
) {
}
