package car.sharing.service.chs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.dto.notication.TelegramMessageRequestDto;
import car.sharing.service.chs.dto.notication.TelegramMessageResponseDto;
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

public class NotificationControllerTest extends BaseControllerTest {
    private static final String ADMIN_CHAT_ID = "8631775085";
    private static final int PAGE_SIZE_SMALL = 1;
    private static final int PAGE_SIZE_LARGE = 10;
    private static final int INITIAL_PAGE = 0;
    private static final int NUMBER_OF_TEST_MESSAGES = 3;
    private static final long SEND_DELAY_MS = 300;
    private static final long HISTORY_DELAY_MS = 500;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        managerToken = TestEntityFactory.registerAndLoginManager(restTemplate, "");
        customerToken = TestEntityFactory.registerAndLoginCustomer(restTemplate, "");
    }

    @Test
    @DisplayName("Send notification as MANAGER - sends message and returns response")
    void send_AsManager_ReturnsSentMessage() {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(
                ADMIN_CHAT_ID, "✅ Test message");
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<TelegramMessageResponseDto> response = restTemplate.exchange(
                "/notifications",
                HttpMethod.POST,
                entity,
                TelegramMessageResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(request.message());
        assertThat(response.getBody().chatId()).isEqualTo(ADMIN_CHAT_ID);
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().sentAt()).isNotNull();
    }

    @Test
    @DisplayName("Send notification as CUSTOMER - returns forbidden")
    void send_AsCustomer_ReturnsForbidden() {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(
                ADMIN_CHAT_ID, "Test message");
        HttpEntity<?> entity = withBodyAndAuth(request, customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/notifications",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Send notification without token - returns unauthorized")
    void send_WithoutToken_ReturnsForbidden() {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(
                ADMIN_CHAT_ID, "Test message");
        HttpEntity<?> entity = new HttpEntity<>(request);

        ResponseEntity<String> response = restTemplate.exchange(
                "/notifications",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Get notification history as MANAGER - returns paginated history")
    void history_AsManager_ReturnsPageOfMessages() throws Exception {
        sendTestMessage("History test message");
        Thread.sleep(HISTORY_DELAY_MS);

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildHistoryUrl(ADMIN_CHAT_ID, INITIAL_PAGE, PAGE_SIZE_LARGE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode content = jsonNode.get("content");

        assertThat(content).isNotNull();
        assertThat(content.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Get notification history with pagination - returns correct page")
    void history_WithPagination_ReturnsCorrectPage() throws Exception {
        sendMultipleTestMessages(NUMBER_OF_TEST_MESSAGES);

        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildHistoryUrl(ADMIN_CHAT_ID, INITIAL_PAGE, PAGE_SIZE_SMALL),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode content = jsonNode.get("content");

        assertThat(content).isNotNull();
        assertThat(content.size()).isEqualTo(PAGE_SIZE_SMALL);
        assertThat(jsonNode.get("totalElements").asInt()).isGreaterThanOrEqualTo(NUMBER_OF_TEST_MESSAGES);
    }

    @Test
    @DisplayName("Get notification history for different chat - returns empty page")
    void history_WithDifferentChatId_ReturnsEmptyPage() throws Exception {
        HttpEntity<?> entity = withAuth(managerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildHistoryUrl("nonExistentChatId", INITIAL_PAGE, PAGE_SIZE_LARGE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode content = jsonNode.get("content");

        assertThat(content).isNotNull();
        assertThat(content.size()).isZero();
        assertThat(jsonNode.get("totalElements").asInt()).isZero();
    }

    @Test
    @DisplayName("Get notification history as CUSTOMER - returns forbidden")
    void history_AsCustomer_ReturnsForbidden() {
        HttpEntity<?> entity = withAuth(customerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                buildHistoryUrl(ADMIN_CHAT_ID, INITIAL_PAGE, PAGE_SIZE_LARGE),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Get notification history without token - returns unauthorized")
    void history_WithoutToken_ReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                buildHistoryUrl(ADMIN_CHAT_ID, INITIAL_PAGE, PAGE_SIZE_LARGE),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Send notification with empty message - returns bad request")
    void send_WithEmptyMessage_ReturnsBadRequest() {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(ADMIN_CHAT_ID, "");
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/notifications",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Send notification with null chatId - returns bad request")
    void send_WithNullChatId_ReturnsBadRequest() {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(null, "Test message");
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/notifications",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void sendTestMessage(String message) {
        TelegramMessageRequestDto request = TestEntityFactory.createTelegramMessageRequest(ADMIN_CHAT_ID, message);
        HttpEntity<?> entity = withBodyAndAuth(request, managerToken);
        restTemplate.exchange("/notifications", HttpMethod.POST, entity, TelegramMessageResponseDto.class);
    }

    private void sendMultipleTestMessages(int count) throws InterruptedException {
        for (int i = 1; i <= count; i++) {
            sendTestMessage("Pagination test " + i);
            Thread.sleep(SEND_DELAY_MS);
        }
    }

    private String buildHistoryUrl(String chatId, int page, int size) {
        return String.format("/notifications?chatId=%s&page=%d&size=%d&sort=sentAt",
                chatId, page, size);
    }
}
