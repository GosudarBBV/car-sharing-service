package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.dto.user.AuthResponseDto;
import car.sharing.service.chs.dto.user.UserLoginRequestDto;
import car.sharing.service.chs.dto.user.UserRegisterRequestDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.util.BaseControllerTest;
import car.sharing.service.chs.util.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AuthControllerTest extends BaseControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerUser_ValidCustomer_ReturnUserResponse() {
        UserRegisterRequestDto request = TestEntityFactory.createValidCustomerRequest();

        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                "/auth/register",
                request,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo(request.email());
        assertThat(response.getBody().firstName()).isEqualTo("John");
        assertThat(response.getBody().lastName()).isEqualTo("Doe");
        assertThat(response.getBody().id()).isNotNull();
    }

    @Test
    void registerUser_ValidManager_ReturnUserResponse() {
        UserRegisterRequestDto request = TestEntityFactory.createValidManagerRequest();

        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                "/auth/register",
                request,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo(request.email());
        assertThat(response.getBody().firstName()).isEqualTo("Jane");
        assertThat(response.getBody().lastName()).isEqualTo("Smith");
        assertThat(response.getBody().id()).isNotNull();
    }

    @Test
    void registerUser_DuplicateEmail_ReturnConflict() {
        UserRegisterRequestDto request = TestEntityFactory.createValidCustomerRequest();

        ResponseEntity<UserResponseDto> firstResponse = restTemplate.postForEntity(
                "/auth/register",
                request,
                UserResponseDto.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                "/auth/register",
                request,
                String.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicateResponse.getBody()).contains("User already exists");
    }

    @Test
    void registerUser_PasswordMismatch_ReturnBadRequest() {
        UserRegisterRequestDto request = TestEntityFactory.createPasswordMismatchRequest();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/register",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Passwords do not match");
    }

    @Test
    void registerUser_InvalidEmail_ReturnBadRequest() {
        UserRegisterRequestDto request = TestEntityFactory.createInvalidEmailRequest();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/register",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Email should be valid");
    }

    @Test
    void login_ValidCredentials_ReturnAuthResponse() {
        // Реєстрація
        UserRegisterRequestDto registerRequest = TestEntityFactory.createValidCustomerRequest();
        ResponseEntity<UserResponseDto> registerResponse = restTemplate.postForEntity(
                "/auth/register",
                registerRequest,
                UserResponseDto.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Логін
        UserLoginRequestDto loginRequest = TestEntityFactory.createUserLoginRequest(
                registerRequest.email(),
                "password123"
        );

        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponseDto.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().token()).isNotEmpty();
    }

    @Test
    void login_InvalidCredentials_ReturnUnauthorized() {
        // Реєстрація
        UserRegisterRequestDto registerRequest = TestEntityFactory.createValidCustomerRequest();
        ResponseEntity<UserResponseDto> registerResponse = restTemplate.postForEntity(
                "/auth/register",
                registerRequest,
                UserResponseDto.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserLoginRequestDto invalidLogin = TestEntityFactory.createInvalidLoginRequest(
                registerRequest.email()
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login",
                invalidLogin,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
