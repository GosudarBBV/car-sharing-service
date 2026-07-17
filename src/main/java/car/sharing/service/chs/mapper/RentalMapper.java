package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "user.id", target = "userId")
    RentalResponseDto toDto(Rental rental);
}
