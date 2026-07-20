package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.user.UserRegisterRequestDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.dto.user.UserUpdateDto;
import car.sharing.service.chs.model.RoleName;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto requestDto);

    void deleteById(Long id);

    UserResponseDto getCurrentUser();

    UserResponseDto updateProfile(UserUpdateDto dto);

    void updateRole(Long id, RoleName role);

    Long getAuthenticatedUserId();

    Long getUserIdByEmail(String email);

    void restoreById(Long userId);
}
