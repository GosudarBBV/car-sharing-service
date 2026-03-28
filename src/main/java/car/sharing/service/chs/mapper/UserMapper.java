package car.sharing.service.chs.mapper;

import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;
import car.sharing.service.chs.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toModel(UserRegisterRequestDto user);

    UserResponseDto toResponseDto(User user);
}