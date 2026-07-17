package car.sharing.service.chs.util;

import car.sharing.service.chs.dto.user.AuthResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.payment.PaymentRequestDto;
import car.sharing.service.chs.dto.notication.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import car.sharing.service.chs.dto.user.UserLoginRequestDto;
import car.sharing.service.chs.dto.user.UserRegisterRequestDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.CarType;
import car.sharing.service.chs.model.Notification;
import car.sharing.service.chs.model.Payment;
import car.sharing.service.chs.model.PaymentStatus;
import car.sharing.service.chs.model.PaymentType;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.model.TelegramMessage;
import car.sharing.service.chs.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

public final class TestEntityFactory {
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_FIRST_NAME = "John";
    private static final String DEFAULT_LAST_NAME = "Doe";
    private static final String DEFAULT_CAR_BRAND = "Tesla";
    private static final String DEFAULT_CAR_MODEL = "Model Y";
    private static final int DEFAULT_CAR_INVENTORY = 5;
    private static final BigDecimal DEFAULT_DAILY_FEE = BigDecimal.valueOf(120.00);
    private static final long DEFAULT_RENTAL_DAYS = 7L;

    private TestEntityFactory() {

    }

    // ========== Authentication Methods ==========

    public static String registerAndLogin(TestRestTemplate restTemplate, RoleName role, String baseUrl) {
        UserRegisterRequestDto registerRequest = createUserRegisterRequestWithRole(role);
        restTemplate.postForEntity(baseUrl + "/auth/register", registerRequest, UserResponseDto.class);

        UserLoginRequestDto loginRequest = createUserLoginRequest(registerRequest.email(), DEFAULT_PASSWORD);
        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginRequest, AuthResponseDto.class);

        return loginResponse.getBody().token();
    }

    public static String registerAndLoginManager(TestRestTemplate restTemplate, String baseUrl) {
        return registerAndLogin(restTemplate, RoleName.MANAGER, baseUrl);
    }

    public static String registerAndLoginCustomer(TestRestTemplate restTemplate, String baseUrl) {
        return registerAndLogin(restTemplate, RoleName.CUSTOMER, baseUrl);
    }

    // ========== Role Methods ==========

    public static Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    // ========== Car Methods ==========

    public static CreateCarRequestDto createCarRequest() {
        return new CreateCarRequestDto(
                DEFAULT_CAR_BRAND,
                DEFAULT_CAR_MODEL,
                CarType.SEDAN,
                DEFAULT_CAR_INVENTORY,
                DEFAULT_DAILY_FEE
        );
    }

    public static UpdateCarRequestDto updateCarRequest() {
        return new UpdateCarRequestDto(
                "Tesla Updated",
                "Model 3",
                CarType.SEDAN,
                10,
                BigDecimal.valueOf(99.99)
        );
    }

    public static Car createCar() {
        Car car = new Car();
        car.setBrand(DEFAULT_CAR_BRAND);
        car.setModel(DEFAULT_CAR_MODEL);
        car.setType(CarType.SEDAN);
        car.setInventory(DEFAULT_CAR_INVENTORY);
        car.setDailyFee(DEFAULT_DAILY_FEE);
        car.setDeleted(false);
        return car;
    }

    // ========== User Methods ==========

    public static UserRegisterRequestDto createUserRegisterRequest() {
        return new UserRegisterRequestDto(
                generateEmail("testuser"),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_PASSWORD,
                DEFAULT_PASSWORD,
                RoleName.CUSTOMER
        );
    }

    public static UserRegisterRequestDto createUserRegisterRequestWithRole(RoleName role) {
        return new UserRegisterRequestDto(
                generateEmail("testuser"),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_PASSWORD,
                DEFAULT_PASSWORD,
                role
        );
    }

    public static UserRegisterRequestDto createValidCustomerRequest() {
        return new UserRegisterRequestDto(
                generateEmail("customer"),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_PASSWORD,
                DEFAULT_PASSWORD,
                RoleName.CUSTOMER
        );
    }

    public static UserRegisterRequestDto createValidManagerRequest() {
        return new UserRegisterRequestDto(
                generateEmail("manager"),
                "Jane",
                "Smith",
                DEFAULT_PASSWORD,
                DEFAULT_PASSWORD,
                RoleName.MANAGER
        );
    }

    public static UserRegisterRequestDto createPasswordMismatchRequest() {
        return new UserRegisterRequestDto(
                generateEmail("test"),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_PASSWORD,
                "differentPassword",
                RoleName.CUSTOMER
        );
    }

    public static UserRegisterRequestDto createInvalidEmailRequest() {
        return new UserRegisterRequestDto(
                "invalid-email",
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_PASSWORD,
                DEFAULT_PASSWORD,
                RoleName.CUSTOMER
        );
    }

    public static UserRegisterRequestDto createEmptyPasswordRequest() {
        return new UserRegisterRequestDto(
                generateEmail("test"),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                "",
                "",
                RoleName.CUSTOMER
        );
    }

    public static UserLoginRequestDto createUserLoginRequest() {
        return new UserLoginRequestDto("testuser@example.com", DEFAULT_PASSWORD);
    }

    public static UserLoginRequestDto createUserLoginRequest(String email, String password) {
        return new UserLoginRequestDto(email, password);
    }

    public static UserLoginRequestDto createInvalidLoginRequest(String email) {
        return new UserLoginRequestDto(email, "wrongpassword");
    }

    public static User createUser() {
        User user = new User();
        user.setEmail(generateEmail("test"));
        user.setFirstName(DEFAULT_FIRST_NAME);
        user.setLastName(DEFAULT_LAST_NAME);
        user.setPassword(DEFAULT_PASSWORD);
        user.setDeleted(false);
        return user;
    }

    public static User createUser(String email) {
        User user = createUser();
        user.setEmail(email);
        return user;
    }

    // ========== Rental Methods ==========

    public static CreateRentalRequestDto createRentalRequest() {
        return new CreateRentalRequestDto(1L, LocalDate.now().plusDays(DEFAULT_RENTAL_DAYS));
    }

    public static CreateRentalRequestDto createRentalRequest(Long carId, LocalDate returnDate) {
        return new CreateRentalRequestDto(carId, returnDate);
    }

    public static CreateRentalRequestDto createRentalRequestWithNullCarId() {
        return new CreateRentalRequestDto(null, LocalDate.now().plusDays(DEFAULT_RENTAL_DAYS));
    }

    public static CreateRentalRequestDto createRentalRequestWithNullReturnDate() {
        return new CreateRentalRequestDto(1L, null);
    }

    public static CreateRentalRequestDto createRentalRequestWithPastReturnDate() {
        return new CreateRentalRequestDto(1L, LocalDate.now().minusDays(1));
    }

    public static CreateRentalRequestDto createRentalRequestWithTodayReturnDate() {
        return new CreateRentalRequestDto(1L, LocalDate.now());
    }

    public static Rental createRental(User user, Car car) {
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(DEFAULT_RENTAL_DAYS));
        rental.setActualReturnDate(null);
        return rental;
    }

    public static Rental createRental(User user, Car car, LocalDate rentalDate, LocalDate returnDate) {
        Rental rental = createRental(user, car);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        return rental;
    }

    // ========== Payment Methods ==========

    public static PaymentRequestDto createPaymentRequest() {
        return new PaymentRequestDto(1L, PaymentType.PAYMENT, DEFAULT_DAILY_FEE);
    }

    public static PaymentRequestDto createPaymentRequest(Long rentalId, PaymentType type, BigDecimal amount) {
        return new PaymentRequestDto(rentalId, type, amount);
    }

    public static PaymentRequestDto createPaymentRequestWithInvalidData() {
        return new PaymentRequestDto(null, PaymentType.PAYMENT, DEFAULT_DAILY_FEE);
    }

    public static PaymentRequestDto createPaymentRequestWithNegativeAmount() {
        return new PaymentRequestDto(1L, PaymentType.PAYMENT, BigDecimal.valueOf(-100.00));
    }

    public static Payment createPayment(Rental rental, PaymentStatus status, PaymentType type) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setStatus(status);
        payment.setType(type);
        payment.setAmount(DEFAULT_DAILY_FEE);
        payment.setSessionId(UUID.randomUUID().toString());
        payment.setSessionUrl("https://checkout.stripe.com/session/" + UUID.randomUUID());
        return payment;
    }

    public static Payment createPayment(Rental rental, PaymentStatus status, PaymentType type, BigDecimal amount) {
        Payment payment = createPayment(rental, status, type);
        payment.setAmount(amount);
        return payment;
    }

    // ========== Notification Methods ==========

    public static TelegramMessageRequestDto createTelegramMessageRequest() {
        return new TelegramMessageRequestDto("123456789", "Test notification message");
    }

    public static TelegramMessageRequestDto createTelegramMessageRequest(String chatId, String message) {
        return new TelegramMessageRequestDto(chatId, message);
    }

    public static TelegramMessage createTelegramMessage(String chatId, String message) {
        TelegramMessage telegramMessage = new TelegramMessage();
        telegramMessage.setChatId(chatId);
        telegramMessage.setMessage(message);
        telegramMessage.setSentAt(LocalDateTime.now());
        return telegramMessage;
    }

    public static Notification createNotification(User user, String message, boolean sent) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setSent(sent);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    // ========== Helper Methods ==========

    private static String generateEmail(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "@example.com";
    }
}
