package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.dto.AuthResponseDto;
import car.sharing.service.chs.dto.UserLoginRequestDto;
import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;
import car.sharing.service.chs.dto.UserUpdateDto;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.util.BaseControllerTest;
import car.sharing.service.chs.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserControllerTest extends BaseControllerTest {

    private static final long NON_EXISTENT_ID = 999L;

    @Autowired
    private TestRestTemplate restTemplate;

    private String managerToken;
    private String customerToken;
    private Long customerId;
    private String customerEmail;

    @BeforeEach
    void setUp() {
        managerToken = TestEntityFactory.registerAndLoginManager(restTemplate, getBaseUrl());

        UserRegisterRequestDto customerRegister = TestEntityFactory.createValidCustomerRequest();
        customerEmail = customerRegister.email();

        ResponseEntity<UserResponseDto> customerRegisterResponse = restTemplate.postForEntity(
                "/auth/register", customerRegister, UserResponseDto.class);
        customerId = customerRegisterResponse.getBody().id();

        UserLoginRequestDto customerLogin = TestEntityFactory.createUserLoginRequest(customerEmail, "password123");
        ResponseEntity<AuthResponseDto> customerLoginResponse = restTemplate.postForEntity(
                "/auth/login", customerLogin, AuthResponseDto.class);
        customerToken = customerLoginResponse.getBody().token();
    }

    @Test
    @DisplayName("Update user role as MANAGER - updates role successfully")
    void updateRole_AsManager_UpdatesRole() {
        HttpEntity<?> entity = withAuth(managerToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/" + customerId + "/role?role=MANAGER",
                HttpMethod.PUT,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Update user role as CUSTOMER - returns forbidden")
    void updateRole_AsCustomer_ReturnsForbidden() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/" + customerId + "/role?role=MANAGER",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update user role without token - returns forbidden")
    void updateRole_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/users/" + customerId + "/role?role=MANAGER",
                HttpMethod.PUT,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update user role for non-existent user - returns not found")
    void updateRole_ForNonExistentUser_ReturnsNotFound() {
        HttpEntity<?> entity = withAuth(managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/" + NON_EXISTENT_ID + "/role?role=MANAGER",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Get current user profile as CUSTOMER - returns profile")
    void getProfile_AsCustomer_ReturnsProfile() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(customerId);
        assertThat(response.getBody().email()).isEqualTo(customerEmail);
    }

    @Test
    @DisplayName("Get current user profile as MANAGER - returns profile")
    void getProfile_AsManager_ReturnsProfile() {
        HttpEntity<?> entity = withAuth(managerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get current user profile without token - returns forbidden")
    void getProfile_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity("/users/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update profile as CUSTOMER - updates profile successfully")
    void updateProfile_AsCustomer_UpdatesProfile() {
        UserUpdateDto updateDto = new UserUpdateDto("UpdatedFirstName", "UpdatedLastName", "newPassword123");
        HttpEntity<?> entity = withBodyAndAuth(updateDto, customerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("UpdatedFirstName");
        assertThat(response.getBody().lastName()).isEqualTo("UpdatedLastName");
    }

    @Test
    @DisplayName("Update profile as MANAGER - updates profile successfully")
    void updateProfile_AsManager_UpdatesProfile() {
        UserUpdateDto updateDto = new UserUpdateDto("ManagerFirstName", "ManagerLastName", null);
        HttpEntity<?> entity = withBodyAndAuth(updateDto, managerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("ManagerFirstName");
        assertThat(response.getBody().lastName()).isEqualTo("ManagerLastName");
    }

    @Test
    @DisplayName("Update profile without token - returns forbidden")
    void updateProfile_WithoutToken_ReturnsForbidden() {
        UserUpdateDto updateDto = new UserUpdateDto("NewName",
                "NewLastName", "newPass");
        HttpEntity<?> entity = new HttpEntity<>(updateDto);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update profile with empty first name - returns bad request")
    void updateProfile_WithEmptyFirstName_ReturnsBadRequest() {
        UserUpdateDto updateDto = new UserUpdateDto("", "LastName", null);
        HttpEntity<?> entity = withBodyAndAuth(updateDto, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Update profile with empty last name - returns bad request")
    void updateProfile_WithEmptyLastName_ReturnsBadRequest() {
        UserUpdateDto updateDto = new UserUpdateDto("FirstName", "", null);
        HttpEntity<?> entity = withBodyAndAuth(updateDto, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Update profile with valid last name only")
    void updateProfile_WithValidLastNameOnly() {
        HttpEntity<?> getEntity = withAuth(customerToken);
        ResponseEntity<UserResponseDto> getResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                getEntity,
                UserResponseDto.class
        );
        String currentFirstName = getResponse.getBody().firstName();

        UserUpdateDto updateDto = new UserUpdateDto(currentFirstName,
                "NewLastNameOnly", null);
        HttpEntity<?> entity = withBodyAndAuth(updateDto, customerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo(currentFirstName);
        assertThat(response.getBody().lastName()).isEqualTo("NewLastNameOnly");
    }

    @Test
    @DisplayName("Delete own account as CUSTOMER - deletes account")
    void deleteMyAccount_AsCustomer_DeletesAccount() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getProfileResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                withAuth(customerToken),
                String.class
        );

        assertThat(getProfileResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Delete own account as MANAGER - deletes account")
    void deleteMyAccount_AsManager_DeletesAccount() {
        UserRegisterRequestDto managerRegister = TestEntityFactory.createUserRegisterRequestWithRole(RoleName.MANAGER);
        ResponseEntity<UserResponseDto> managerRegisterResponse = restTemplate.postForEntity(
                "/auth/register", managerRegister, UserResponseDto.class);
        Long newManagerId = managerRegisterResponse.getBody().id();

        UserLoginRequestDto managerLogin = TestEntityFactory.createUserLoginRequest(managerRegister.email(), "password123");
        ResponseEntity<AuthResponseDto> managerLoginResponse = restTemplate.postForEntity(
                "/auth/login", managerLogin, AuthResponseDto.class);
        String tempManagerToken = managerLoginResponse.getBody().token();

        HttpEntity<?> entity = withAuth(tempManagerToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getProfileResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                withAuth(tempManagerToken),
                String.class
        );

        assertThat(getProfileResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Delete own account without token - returns forbidden")
    void deleteMyAccount_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update profile with password - updates password successfully")
    void updateProfile_WithPassword_UpdatesPassword() {
        HttpEntity<?> getEntity = withAuth(customerToken);
        ResponseEntity<UserResponseDto> getResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                getEntity,
                UserResponseDto.class
        );
        String currentFirstName = getResponse.getBody().firstName();
        String currentLastName = getResponse.getBody().lastName();

        UserUpdateDto updateDto = new UserUpdateDto(currentFirstName, currentLastName,
                "newSecurePassword123");
        HttpEntity<?> entity = withBodyAndAuth(updateDto, customerToken);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                entity,
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        UserLoginRequestDto loginRequest = new UserLoginRequestDto(customerEmail,
                "newSecurePassword123");
        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, AuthResponseDto.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().token()).isNotBlank();
    }
}
