package car.sharing.service.chs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.util.BaseRepositoryTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

class RoleRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Find role by name - returns role when exists")
    void findByName_shouldReturnRole() {
        Optional<Role> result = roleRepository.findByName(RoleName.CUSTOMER);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleName.CUSTOMER);
    }

    @Test
    @DisplayName("Find role by name - returns empty when role does not exist")
    void findByName_notFound_shouldReturnEmpty() {
        Optional<Role> result = roleRepository.findByName(null);

        assertThat(result).isEmpty();
    }
}
