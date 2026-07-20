package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.notication.TelegramMessageResponseDto;
import car.sharing.service.chs.model.TelegramMessage;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface TelegramMessageMapper {
    TelegramMessageResponseDto toDto(TelegramMessage entity);
}
