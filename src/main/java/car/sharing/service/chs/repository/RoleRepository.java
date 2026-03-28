package car.sharing.service.chs.repository;

import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
