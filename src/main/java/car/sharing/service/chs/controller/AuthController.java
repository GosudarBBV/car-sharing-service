package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.AuthResponseDto;
import car.sharing.service.chs.dto.UserLoginRequestDto;
import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;
import car.sharing.service.chs.security.AuthenticationService;
import car.sharing.service.chs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authService;

    @PostMapping("/registration")
    @Operation(summary = "Register new user")
    public UserResponseDto registerUser(
            @RequestBody @Valid UserRegisterRequestDto requestDto
    ) {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and return JWT token")
    public AuthResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto
    ) {
        return authService.authenticate(requestDto);
    }
}
