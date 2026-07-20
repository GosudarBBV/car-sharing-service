package car.sharing.service.chs.dto.user;

import car.sharing.service.chs.model.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
        @Schema(example = "test@gmail.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @Schema(example = "John")
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Schema(example = "Doe")
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Schema(example = "password123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,

        @Schema(example = "password123")
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @Schema(example = "MANAGER")
        @NotNull(message = "Role is required")
        RoleName role
) {}
