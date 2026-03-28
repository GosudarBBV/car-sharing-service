package car.sharing.service.chs.service;
import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto requestDto);

    void deleteById(Long id);
}
