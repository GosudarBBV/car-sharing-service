package car.sharing.service.chs.dto;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {}