package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.payment.PaymentRequestDto;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.model.PaymentType;
import car.sharing.service.chs.util.BaseControllerTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.math.BigDecimal;
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

public class PaymentControllerTest extends BaseControllerTest {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int PAGINATION_SIZE = 5;
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("120.00");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");
    private static final String TEST_SESSION_ID = "test_session_123";
    private static final String NON_EXISTENT_SESSION = "non_existent_session_123";

    @Autowired
    private TestRestTemplate restTemplate;

    private String managerToken;
    private String customerToken;
    private Long testCarId;
    private Long testRentalId;

    @BeforeEach
    void setUp() {
        managerToken = TestEntityFactory.registerAndLoginManager(restTemplate, getBaseUrl());
        customerToken = TestEntityFactory.registerAndLoginCustomer(restTemplate, getBaseUrl());

        testCarId = createTestCar();
        testRentalId = createTestRental();
    }

    @Test
    @DisplayName("Get payments as CUSTOMER - returns page of payments")
    void getPayments_AsCustomer_ReturnsPageOfPayments() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                buildPaymentsUrl(DEFAULT_PAGE, DEFAULT_SIZE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get payments as MANAGER - returns page of payments")
    void getPayments_AsManager_ReturnsPageOfPayments() {
        HttpEntity<?> entity = withAuth(managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                buildPaymentsUrl(DEFAULT_PAGE, DEFAULT_SIZE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Get payments without token - returns forbidden")
    void getPayments_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                buildPaymentsUrl(DEFAULT_PAGE, DEFAULT_SIZE),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Create payment as CUSTOMER - returns payment response")
    void createPayment_AsCustomer_ReturnsPaymentResponse() {
        PaymentRequestDto request = createPaymentRequest(PAYMENT_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        System.err.println("STATUS: " + response.getStatusCode());
        System.err.println("BODY: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Create payment as MANAGER - returns forbidden")
    void createPayment_AsManager_ReturnsForbidden() {
        PaymentRequestDto request = createPaymentRequest(PAYMENT_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Create payment without token - returns forbidden")
    void createPayment_WithoutToken_ReturnsForbidden() {
        PaymentRequestDto request = createPaymentRequest(PAYMENT_AMOUNT);
        HttpEntity<?> entity = new HttpEntity<>(request);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Create payment with invalid rentalId - returns bad request")
    void createPayment_WithInvalidRentalId_ReturnsBadRequest() {
        PaymentRequestDto request = new PaymentRequestDto(null, PaymentType.PAYMENT, PAYMENT_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Create payment with negative amount - returns bad request")
    void createPayment_WithNegativeAmount_ReturnsBadRequest() {
        PaymentRequestDto request = createPaymentRequest(NEGATIVE_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Create payment with zero amount - returns bad request")
    void createPayment_WithZeroAmount_ReturnsBadRequest() {
        PaymentRequestDto request = createPaymentRequest(BigDecimal.ZERO);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Success endpoint - returns success message")
    void success_ReturnsSuccessMessage() {
        ResponseEntity<String> response = restTemplate.getForEntity("/payments/success", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Payment processed successfully");
    }

    @Test
    @DisplayName("Cancel endpoint - returns cancel message")
    void cancel_ReturnsCancelMessage() {
        ResponseEntity<String> response = restTemplate.getForEntity("/payments/cancel", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Payment was canceled");
    }

    @Test
    @DisplayName("Cancel payment as CUSTOMER - returns success message")
    void cancelPayment_AsCustomer_ReturnsSuccessMessage() {
        String sessionId = createPaymentAndGetSessionId();

        HttpEntity<?> cancelEntity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildCancelPaymentUrl(sessionId),
                HttpMethod.POST,
                cancelEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Payment canceled");
    }

    @Test
    @DisplayName("Cancel payment as MANAGER - returns forbidden")
    void cancelPayment_AsManager_ReturnsForbidden() {
        HttpEntity<?> entity = withAuth(managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                buildCancelPaymentUrl(TEST_SESSION_ID),
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Cancel payment without token - returns forbidden")
    void cancelPayment_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                buildCancelPaymentUrl(TEST_SESSION_ID),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Cancel payment with non-existent session - returns not found")
    void cancelPayment_WithNonExistentSession_ReturnsNotFound() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                buildCancelPaymentUrl(NON_EXISTENT_SESSION),
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Get payments with pagination - returns correct page")
    void getPayments_WithPagination_ReturnsCorrectPage() {
        HttpEntity<?> entity = withAuth(customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                buildPaymentsUrl(DEFAULT_PAGE, PAGINATION_SIZE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Create payment - generates real Stripe checkout session for manual testing")
    void createPayment_GenerateStripeCheckoutUrl() {
        PaymentRequestDto request = createPaymentRequest(PAYMENT_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String sessionUrl = extractSessionUrl(response.getBody());

        printStripeCheckoutUrl(sessionUrl);

        assertThat(sessionUrl).contains("checkout.stripe.com");
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
        CreateRentalRequestDto request = TestEntityFactory.createRentalRequest(testCarId, LocalDate.now().plusDays(7));
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

    private PaymentRequestDto createPaymentRequest(BigDecimal amount) {
        return new PaymentRequestDto(testRentalId, PaymentType.PAYMENT, amount);
    }

    private String createPaymentAndGetSessionId() {
        PaymentRequestDto request = createPaymentRequest(PAYMENT_AMOUNT);
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return extractSessionId(response.getBody());
    }

    private String buildPaymentsUrl(int page, int size) {
        return String.format("/payments?page=%d&size=%d", page, size);
    }

    private String buildCancelPaymentUrl(String sessionId) {
        return String.format("/payments/cancel?session_id=%s", sessionId);
    }

    private String extractSessionId(String jsonResponse) {
        int sessionIdIndex = jsonResponse.indexOf("\"sessionId\"");
        if (sessionIdIndex == -1) {
            sessionIdIndex = jsonResponse.indexOf("\"session_id\"");
        }
        int startQuote = jsonResponse.indexOf("\"", sessionIdIndex + 12) + 1;
        int endQuote = jsonResponse.indexOf("\"", startQuote);
        return jsonResponse.substring(startQuote, endQuote);
    }

    private String extractSessionUrl(String jsonResponse) {
        String target = "\"sessionUrl\":\"";
        int startIndex = jsonResponse.indexOf(target);
        if (startIndex == -1) {
            target = "\"session_url\":\"";
            startIndex = jsonResponse.indexOf(target);
        }
        int startQuote = startIndex + target.length();
        int endQuote = jsonResponse.indexOf("\"", startQuote);
        return jsonResponse.substring(startQuote, endQuote);
    }

    private void printStripeCheckoutUrl(String sessionUrl) {
        System.out.println("\n========================================");
        System.out.println("🔗 Stripe Checkout URL для тестування оплати:");
        System.out.println(sessionUrl);
        System.out.println("💳 Test card: 4242 4242 4242 4242");
        System.out.println("📅 Expiry: 12/34");
        System.out.println("🔐 CVC: 123");
        System.out.println("========================================\n");
    }
}