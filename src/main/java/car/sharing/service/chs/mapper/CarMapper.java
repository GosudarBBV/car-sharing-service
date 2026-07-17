package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import car.sharing.service.chs.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    Car toEntity(CreateCarRequestDto dto);

    CarResponseDto toDto(Car car);

    void updateCarFromDto(UpdateCarRequestDto dto, @MappingTarget Car car);
}
