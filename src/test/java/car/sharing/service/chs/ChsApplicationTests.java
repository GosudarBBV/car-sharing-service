package car.sharing.service.chs;

import car.sharing.service.chs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ChsApplicationTests {

    @MockBean
    private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
