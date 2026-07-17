package car.sharing.service.chs.dto.user;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {}
