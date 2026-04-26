package car.sharing.service.chs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.util.BaseRepositoryTest;
import car.sharing.service.chs.util.TestEntityFactory;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

class UserRepositoryTest extends BaseRepositoryTest {

    private static final String NON_EXISTING_EMAIL = "nonexisting@example.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Check if email exists - returns true when email exists")
    void existsByEmail_existingEmail_returnsTrue() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail(user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Check if email exists - returns false when email does not exist")
    void existsByEmail_nonExistingEmail_returnsFalse() {
        boolean exists = userRepository.existsByEmail(NON_EXISTING_EMAIL);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Find user by email - returns user when email exists")
    void findByEmail_existingEmail_returnsUser() {
        User user = TestEntityFactory.createUser();
        entityManager.persistAndFlush(user);

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Find user by email - returns empty when email does not exist")
    void findByEmail_nonExistingEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail(NON_EXISTING_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find user with roles by email - returns user with roles when email exists")
    void findWithRolesByEmail_existingEmail_returnsUserWithRoles() {
        Role existingRole = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", RoleName.CUSTOMER)
                .getResultStream()
                .findFirst()
                .orElse(null);

        User user = TestEntityFactory.createUser();
        if (existingRole != null) {
            user.setRoles(Set.of(existingRole));
        }
        entityManager.persistAndFlush(user);

        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findWithRolesByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Find user with roles by email - returns empty when email does not exist")
    void findWithRolesByEmail_nonExistingEmail_returnsEmpty() {
        Optional<User> result = userRepository.findWithRolesByEmail(NON_EXISTING_EMAIL);

        assertThat(result).isEmpty();
    }
}
