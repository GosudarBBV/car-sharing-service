package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import car.sharing.service.chs.service.CarService;
import car.sharing.service.chs.util.BaseControllerTest;
import car.sharing.service.chs.util.TestEntityFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CarControllerTest extends BaseControllerTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final long NON_EXISTENT_ID = 999L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CarService carService;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        managerToken = TestEntityFactory.registerAndLoginManager(restTemplate, getBaseUrl());
        customerToken = TestEntityFactory.registerAndLoginCustomer(restTemplate, getBaseUrl());
    }

    @Test
    @DisplayName("Get all cars as MANAGER - returns paginated list of cars")
    void getAll_AsManager_ReturnsPageOfCars() throws Exception {
        createTestCar();

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildCarsUrl(DEFAULT_PAGE, DEFAULT_SIZE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode content = jsonNode.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Get all cars as CUSTOMER - returns paginated list of cars")
    void getAll_AsCustomer_ReturnsPageOfCars() throws Exception {
        createTestCar();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildCarsUrl(DEFAULT_PAGE, DEFAULT_SIZE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode content = jsonNode.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Get car by ID - returns car when exists")
    void getById_WhenCarExists_ReturnsCar() {
        CreateCarRequestDto createRequest = TestEntityFactory.createCarRequest();
        CarResponseDto createdCar = carService.create(createRequest);
        Long carId = createdCar.id();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<CarResponseDto> response = restTemplate.exchange(
                "/cars/" + carId,
                HttpMethod.GET,
                entity,
                CarResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(carId);
        assertThat(response.getBody().brand()).isEqualTo(createRequest.brand());
        assertThat(response.getBody().model()).isEqualTo(createRequest.model());
    }

    @Test
    @DisplayName("Create car as MANAGER - creates and returns car")
    void create_AsManager_ReturnsCreatedCar() {
        CreateCarRequestDto request = TestEntityFactory.createCarRequest();
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<CarResponseDto> response = restTemplate.exchange(
                "/cars",
                HttpMethod.POST,
                entity,
                CarResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertCarResponseMatchesRequest(response.getBody(), request);
        assertThat(response.getBody().id()).isNotNull();

        CarResponseDto savedCar = carService.getById(response.getBody().id());
        assertThat(savedCar).isNotNull();
        assertThat(savedCar.brand()).isEqualTo(request.brand());
    }

    @Test
    @DisplayName("Create car as CUSTOMER - returns forbidden")
    void create_AsCustomer_ReturnsForbidden() {
        CreateCarRequestDto request = TestEntityFactory.createCarRequest();
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/cars",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Update car as MANAGER - updates and returns car")
    void update_AsManager_ReturnsUpdatedCar() {
        CarResponseDto createdCar = createTestCar();
        Long carId = createdCar.id();

        UpdateCarRequestDto updateRequest = TestEntityFactory.updateCarRequest();
        HttpEntity<?> entity = withBodyAndAuth(updateRequest, managerToken);

        ResponseEntity<CarResponseDto> response = restTemplate.exchange(
                "/cars/" + carId,
                HttpMethod.PUT,
                entity,
                CarResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertCarResponseMatchesUpdateRequest(response.getBody(), updateRequest);

        CarResponseDto updatedCar = carService.getById(carId);
        assertThat(updatedCar).isNotNull();
        assertThat(updatedCar.brand()).isEqualTo(updateRequest.brand());
    }

    @Test
    @DisplayName("Update car as CUSTOMER - returns forbidden")
    void update_AsCustomer_ReturnsForbidden() {
        CarResponseDto createdCar = createTestCar();
        Long carId = createdCar.id();

        UpdateCarRequestDto updateRequest = TestEntityFactory.updateCarRequest();
        HttpEntity<?> entity = withBodyAndAuth(updateRequest, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/cars/" + carId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Delete car as MANAGER - soft deletes car")
    void delete_AsManager_ReturnsNoContent() {
        CarResponseDto createdCar = createTestCar();
        Long carId = createdCar.id();

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/cars/" + carId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThatThrownBy(() -> carService.getById(carId))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Delete car as CUSTOMER - returns forbidden")
    void delete_AsCustomer_ReturnsForbidden() {
        CarResponseDto createdCar = createTestCar();
        Long carId = createdCar.id();

        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                "/cars/" + carId,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Get car by ID when car does not exist - returns not found")
    void getById_WhenCarDoesNotExist_ReturnsNotFound() {
        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                "/cars/" + NON_EXISTENT_ID,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private CarResponseDto createTestCar() {
        CreateCarRequestDto request = TestEntityFactory.createCarRequest();
        return carService.create(request);
    }

    private String buildCarsUrl(int page, int size) {
        return String.format("/cars?page=%d&size=%d", page, size);
    }

    private void assertCarResponseMatchesRequest(CarResponseDto response, CreateCarRequestDto request) {
        assertThat(response.brand()).isEqualTo(request.brand());
        assertThat(response.model()).isEqualTo(request.model());
        assertThat(response.type()).isEqualTo(request.type());
        assertThat(response.inventory()).isEqualTo(request.inventory());
        assertThat(response.dailyFee()).isEqualTo(request.dailyFee());
    }

    private void assertCarResponseMatchesUpdateRequest(CarResponseDto response, UpdateCarRequestDto request) {
        assertThat(response.brand()).isEqualTo(request.brand());
        assertThat(response.model()).isEqualTo(request.model());
        assertThat(response.inventory()).isEqualTo(request.inventory());
        assertThat(response.dailyFee()).isEqualTo(request.dailyFee());
    }
}
