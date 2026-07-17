package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.payment.PaymentResponseDto;
import car.sharing.service.chs.model.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toDto(Payment payment);
}
