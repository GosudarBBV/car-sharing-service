package car.sharing.service.chs.dto;

import car.sharing.service.chs.model.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserRegisterRequestDto(
        @Schema(example = "test@gmail.com")
        String email,

        @Schema(example = "John")
        String firstName,

        @Schema(example = "Doe")
        String lastName,

        @Schema(example = "password123")
        String password,

        @Schema(example = "password123")
        String confirmPassword,

        @Schema(example = "MANAGER")
        RoleName role
) {}