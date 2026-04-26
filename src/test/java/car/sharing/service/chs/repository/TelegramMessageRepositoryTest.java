package car.sharing.service.chs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.model.TelegramMessage;
import car.sharing.service.chs.util.BaseRepositoryTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TelegramMessageRepositoryTest extends BaseRepositoryTest {
    private static final String EXISTING_CHAT_ID = "chat123";
    private static final String SECOND_CHAT_ID = "chat456";
    private static final String NON_EXISTING_CHAT_ID = "nonExisting";
    private static final String CHAT_ID_ONE = "chat1";
    private static final String CHAT_ID_TWO = "chat2";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int TOTAL_MESSAGES = 15;
    private static final int FIRST_PAGE_SIZE = 10;

    @Autowired
    private TelegramMessageRepository telegramMessageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Find all messages by chat id - returns messages page when chat exists")
    void findAllByChatId_existingChatId_returnsMessagesPage() {
        TelegramMessage message1 = TestEntityFactory
                .createTelegramMessage(EXISTING_CHAT_ID, "Hello");
        TelegramMessage message2 = TestEntityFactory
                .createTelegramMessage(EXISTING_CHAT_ID, "World");
        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<TelegramMessage> result = telegramMessageRepository
                .findAllByChatId(EXISTING_CHAT_ID, pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Find all messages by chat id - returns empty page when chat does not exist")
    void findAllByChatId_nonExistingChatId_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<TelegramMessage> result = telegramMessageRepository
                .findAllByChatId(NON_EXISTING_CHAT_ID, pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Find all messages by chat id with pagination - returns correct page")
    void findAllByChatId_withPagination_returnsCorrectPage() {
        IntStream.rangeClosed(1, TOTAL_MESSAGES).forEach(i -> {
            TelegramMessage message = TestEntityFactory.createTelegramMessage(
                    SECOND_CHAT_ID, "Message " + i);
            entityManager.persist(message);
        });
        entityManager.flush();

        Pageable firstPage = PageRequest.of(DEFAULT_PAGE, FIRST_PAGE_SIZE);
        Page<TelegramMessage> result = telegramMessageRepository
                .findAllByChatId(SECOND_CHAT_ID, firstPage);

        assertThat(result.getTotalElements()).isEqualTo(TOTAL_MESSAGES);
        assertThat(result.getContent().size()).isEqualTo(FIRST_PAGE_SIZE);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumber()).isEqualTo(DEFAULT_PAGE);
    }

    @Test
    @DisplayName("Find all messages by chat id for multiple chats "
            + "- returns only specific chat messages")
    void findAllByChatId_multipleChats_returnsOnlySpecificChatMessages() {
        TelegramMessage message1 = TestEntityFactory.createTelegramMessage(CHAT_ID_ONE,
                "Hello1");
        TelegramMessage message2 = TestEntityFactory.createTelegramMessage(CHAT_ID_TWO,
                "Hello2");
        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<TelegramMessage> result = telegramMessageRepository.findAllByChatId(CHAT_ID_ONE, pageable);

        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getChatId()).isEqualTo(CHAT_ID_ONE);
    }
}
