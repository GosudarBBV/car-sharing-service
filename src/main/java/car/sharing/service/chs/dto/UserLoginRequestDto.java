package car.sharing.service.chs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserLoginRequestDto(
        @Schema(example = "test@gmail.com")
        String email,

        @Schema(example = "password123")
        String password
) {}
