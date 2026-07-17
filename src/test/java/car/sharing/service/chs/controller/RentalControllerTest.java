package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.util.BaseControllerTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RentalControllerTest extends BaseControllerTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int SMALL_SIZE = 3;
    private static final int NUMBER_OF_RENTALS = 5;
    private static final long DAYS_TO_ADD = 7L;

    @Autowired
    private TestRestTemplate restTemplate;

    private String managerToken;
    private String customerToken;
    private Long customerUserId;
    private Long testCarId;

    @BeforeEach
    void setUp() {
        managerToken = TestEntityFactory.registerAndLoginManager(restTemplate, getBaseUrl());
        customerToken = TestEntityFactory.registerAndLoginCustomer(restTemplate, getBaseUrl());

        testCarId = createTestCar();

        HttpEntity<?> getProfileEntity = withAuth(customerToken);
        ResponseEntity<UserResponseDto> profileResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                getProfileEntity,
                UserResponseDto.class
        );
        customerUserId = profileResponse.getBody().id();
    }

    @Test
    @DisplayName("Create rental as CUSTOMER - returns rental response")
    void createRental_AsCustomer_ReturnsRentalResponse() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequest(testCarId, LocalDate.now().plusDays(DAYS_TO_ADD));
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().carId()).isEqualTo(testCarId);
        assertThat(response.getBody().userId()).isEqualTo(customerUserId);
    }

    @Test
    @DisplayName("Create rental as MANAGER - returns forbidden")
    void createRental_AsManager_ReturnsForbidden() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequest(testCarId, LocalDate.now().plusDays(DAYS_TO_ADD));
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Create rental without token - returns forbidden")
    void createRental_WithoutToken_ReturnsForbidden() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequest(testCarId, LocalDate.now().plusDays(DAYS_TO_ADD));
        HttpEntity<?> entity = new HttpEntity<>(request);

        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Create rental with null carId - returns bad request")
    void createRental_WithNullCarId_ReturnsBadRequest() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequestWithNullCarId();
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Create rental with null returnDate - returns bad request")
    void createRental_WithNullReturnDate_ReturnsBadRequest() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequestWithNullReturnDate();
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Create rental with past return date - returns bad request")
    void createRental_WithPastReturnDate_ReturnsBadRequest() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequestWithPastReturnDate();
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Create rental with today as return date - returns success")
    void createRental_WithTodayReturnDate_ReturnsResponse() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequestWithTodayReturnDate();
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rentals as CUSTOMER - returns page of rentals")
    void getRentals_AsCustomer_ReturnsPageOfRentals() {
        createTestRental();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildRentalsUrl(DEFAULT_PAGE, DEFAULT_SIZE, null),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rentals as MANAGER - returns page of rentals")
    void getRentals_AsManager_ReturnsPageOfRentals() {
        createTestRental();

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildRentalsUrl(DEFAULT_PAGE, DEFAULT_SIZE, null),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rentals with isActive filter - returns filtered rentals")
    void getRentals_WithIsActiveFilter_ReturnsFilteredRentals() {
        createTestRental();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildRentalsUrl(DEFAULT_PAGE, DEFAULT_SIZE, true),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rentals with isActive=false filter - returns filtered rentals")
    void getRentals_WithIsActiveFalseFilter_ReturnsFilteredRentals() {
        Long rentalId = createAndReturnTestRental();

        // Повертаємо оренду
        HttpEntity<?> returnEntity = withAuth(customerToken);
        restTemplate.exchange("/rentals/" + rentalId + "/return",
                HttpMethod.POST, returnEntity, RentalResponseDto.class);

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildRentalsUrl(DEFAULT_PAGE, DEFAULT_SIZE, false),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rentals without token - returns forbidden")
    void getRentals_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                buildRentalsUrl(DEFAULT_PAGE, DEFAULT_SIZE, null),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Get rental by ID as CUSTOMER - returns rental")
    void getRentalById_AsCustomer_ReturnsRental() {
        Long rentalId = createTestRental();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals/" + rentalId,
                HttpMethod.GET,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(rentalId);
    }

    @Test
    @DisplayName("Get rental by ID as MANAGER - returns rental")
    void getRentalById_AsManager_ReturnsRental() {
        Long rentalId = createTestRental();

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals/" + rentalId,
                HttpMethod.GET,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get rental by ID without token - returns forbidden")
    void getRentalById_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity("/rentals/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Return rental as CUSTOMER - returns updated rental")
    void returnRental_AsCustomer_ReturnsUpdatedRental() {
        Long rentalId = createTestRental();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals/" + rentalId + "/return",
                HttpMethod.POST,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().actualReturnDate()).isNotNull();
    }

    @Test
    @DisplayName("Return rental as MANAGER - returns forbidden")
    void returnRental_AsManager_ReturnsForbidden() {
        Long rentalId = createTestRental();

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                "/rentals/" + rentalId + "/return",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Return rental without token - returns forbidden")
    void returnRental_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rentals/1/return",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Get rentals with pagination - returns correct page")
    void getRentals_WithPagination_ReturnsCorrectPage() {
        // Створюємо кілька оренд
        for (int i = 0; i < NUMBER_OF_RENTALS; i++) {
            createTestRental();
        }

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildRentalsUrl(DEFAULT_PAGE, SMALL_SIZE, null),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    private Long createTestCar() {
        CreateCarRequestDto request = TestEntityFactory.createCarRequest();
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<CarResponseDto> response = restTemplate.exchange(
                "/cars",
                HttpMethod.POST,
                entity,
                CarResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private Long createTestRental() {
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequest(testCarId, LocalDate.now().plusDays(DAYS_TO_ADD));
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<RentalResponseDto> response = restTemplate.exchange(
                "/rentals",
                HttpMethod.POST,
                entity,
                RentalResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().id();
    }

    private Long createAndReturnTestRental() {
        Long rentalId = createTestRental();

        HttpEntity<?> returnEntity = withAuth(customerToken);
        restTemplate.exchange("/rentals/" + rentalId + "/return",
                HttpMethod.POST, returnEntity, RentalResponseDto.class);

        return rentalId;
    }

    private String buildRentalsUrl(int page, int size, Boolean isActive) {
        String url = String.format("/rentals?page=%d&size=%d", page, size);
        if (isActive != null) {
            url += "&isActive=" + isActive;
        }
        return url;
    }
}