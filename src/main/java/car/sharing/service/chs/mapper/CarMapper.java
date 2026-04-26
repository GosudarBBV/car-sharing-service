package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.CarResponseDto;
import car.sharing.service.chs.dto.CreateCarRequestDto;
import car.sharing.service.chs.dto.UpdateCarRequestDto;
import car.sharing.service.chs.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    Car toEntity(CreateCarRequestDto dto);

    CarResponseDto toDto(Car car);

    void updateCarFromDto(UpdateCarRequestDto dto, @MappingTarget Car car);
}
