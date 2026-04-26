package car.sharing.service.chs.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import car.sharing.service.chs.model.Notification;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.util.BaseRepositoryTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.util.stream.IntStream;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class NotificationRepositoryTest extends BaseRepositoryTest {

    private static final long NON_EXISTENT_USER_ID = 999L;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int SECOND_PAGE = 1;
    private static final int PAGINATION_SIZE = 10;
    private static final int TOTAL_NOTIFICATIONS = 15;
    private static final int TOTAL_UNSENT_NOTIFICATIONS = 25;
    private static final int LARGE_PAGE_SIZE = 20;
    private static final int NUMBER_OF_SENT_NOTIFICATIONS = 5;
    private static final int NUMBER_OF_UNSENT_NOTIFICATIONS = 5;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Find all notifications by user id - returns page of notifications")
    void findAllByUserId_existingUserId_returnsNotificationsPage() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        Notification notification1 = TestEntityFactory.createNotification(user,
                "Message 1", false);
        Notification notification2 = TestEntityFactory.createNotification(user,
                "Message 2", true);
        entityManager.persistAndFlush(notification1);
        entityManager.persistAndFlush(notification2);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository.findAllByUserId(user.getId(),
                pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Find all notifications by non-existent user id - returns empty page")
    void findAllByUserId_nonExistingUserId_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository
                .findAllByUserId(NON_EXISTENT_USER_ID, pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Find all notifications by user id with pagination - returns correct page")
    void findAllByUserId_withPagination_returnsCorrectPage() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        IntStream.rangeClosed(1, TOTAL_NOTIFICATIONS).forEach(i -> {
            Notification notification = TestEntityFactory.createNotification(
                    user, "Message " + i, false);
            entityManager.persist(notification);
        });
        entityManager.flush();

        Pageable firstPage = PageRequest.of(DEFAULT_PAGE, PAGINATION_SIZE);
        Page<Notification> result = notificationRepository.findAllByUserId(user.getId(), firstPage);

        assertThat(result.getTotalElements()).isEqualTo(TOTAL_NOTIFICATIONS);
        assertThat(result.getContent().size()).isEqualTo(PAGINATION_SIZE);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumber()).isEqualTo(DEFAULT_PAGE);
    }

    @Test
    @DisplayName("Find all notifications by user id for multiple users - returns only specific user notifications")
    void findAllByUserId_multipleUsers_returnsOnlySpecificUserNotifications() {
        User user1 = TestEntityFactory.createUser();
        User user2 = TestEntityFactory.createUser();

        user1.setEmail("user1_" + System.currentTimeMillis() + "@example.com");
        user2.setEmail("user2_" + System.currentTimeMillis() + "@example.com");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        Notification user1Notif = TestEntityFactory.createNotification(user1,
                "User1 message", false);
        Notification user2Notif = TestEntityFactory.createNotification(user2,
                "User2 message", false);
        entityManager.persistAndFlush(user1Notif);
        entityManager.persistAndFlush(user2Notif);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository.findAllByUserId(user1.getId(), pageable);

        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMessage()).isEqualTo("User1 message");
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("Find all notifications by user id when user is deleted - returns empty page")
    void findAllByUserId_userIsDeleted_returnsEmptyPage() {
        User user = TestEntityFactory.createUser();
        user.setDeleted(true);
        entityManager.persistAndFlush(user);

        Notification notification = TestEntityFactory
                .createNotification(user, "Message", false);
        entityManager.persistAndFlush(notification);

        entityManager.flush();
        entityManager.clear();


        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository.findAllByUserId(user.getId(), pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Find all unsent notifications - returns unsent notifications")
    void findAllBySentFalse_withSentFalseNotifications_returnsNotifications() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        Notification sentNotif = TestEntityFactory.createNotification(user,
                "Sent message", true);
        Notification unsentNotif1 = TestEntityFactory.createNotification(user,
                "Unsent message 1", false);
        Notification unsentNotif2 = TestEntityFactory.createNotification(user,
                "Unsent message 2", false);
        entityManager.persistAndFlush(sentNotif);
        entityManager.persistAndFlush(unsentNotif1);
        entityManager.persistAndFlush(unsentNotif2);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository.findAllBySentFalse(pageable);

        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().stream().allMatch(n -> !n.isSent())).isTrue();
    }

    @Test
    @DisplayName("Find all unsent notifications when none exist - returns empty page")
    void findAllBySentFalse_withNoUnsentNotifications_returnsEmptyPage() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        Notification sentNotif1 = TestEntityFactory.createNotification(user,
                "Sent message 1", true);
        Notification sentNotif2 = TestEntityFactory.createNotification(user,
                "Sent message 2", true);
        entityManager.persistAndFlush(sentNotif1);
        entityManager.persistAndFlush(sentNotif2);

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Page<Notification> result = notificationRepository.findAllBySentFalse(pageable);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Find all unsent notifications with pagination - returns correct page")
    void findAllBySentFalse_withPagination_returnsCorrectPage() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        IntStream.rangeClosed(1, TOTAL_UNSENT_NOTIFICATIONS).forEach(i -> {
            Notification notification = TestEntityFactory.createNotification(
                    user, "Unsent message " + i, false);
            entityManager.persist(notification);
        });
        entityManager.flush();

        Pageable pageable = PageRequest.of(SECOND_PAGE, PAGINATION_SIZE);
        Page<Notification> result = notificationRepository.findAllBySentFalse(pageable);

        assertThat(result.getTotalElements()).isEqualTo(TOTAL_UNSENT_NOTIFICATIONS);
        assertThat(result.getContent().size()).isEqualTo(PAGINATION_SIZE);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumber()).isEqualTo(SECOND_PAGE);
    }

    @Test
    @DisplayName("Find all unsent notifications with mixed sent and unsent - returns only unsent")
    void findAllBySentFalse_mixedSentAndUnsent_returnsOnlyUnsent() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        for (int i = 1; i <= NUMBER_OF_SENT_NOTIFICATIONS; i++) {
            Notification sent = TestEntityFactory.createNotification(user,
                    "Sent " + i, true);
            Notification unsent = TestEntityFactory.createNotification(user,
                    "Unsent " + i, false);
            entityManager.persist(sent);
            entityManager.persist(unsent);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, LARGE_PAGE_SIZE);
        Page<Notification> result = notificationRepository.findAllBySentFalse(pageable);

        assertThat(result.getContent().size()).isEqualTo(NUMBER_OF_UNSENT_NOTIFICATIONS);
        assertThat(result.getContent().stream().allMatch(n
                -> !n.isSent())).isTrue();
    }
}
