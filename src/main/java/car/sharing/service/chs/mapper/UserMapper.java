package car.sharing.service.chs.mapper;

import car.sharing.service.chs.config.MapperConfig;
import car.sharing.service.chs.dto.user.UserRegisterRequestDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegisterRequestDto user);

    UserResponseDto toResponseDto(User user);
}
