package car.sharing.service.chs.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class CustomMySqlContainer {
    private static final Dotenv dotenv;
    private static final MySQLContainer<?> mysqlContainer;

    static {
        dotenv = Dotenv.configure()
                .directory("src/test/resources")
                .filename("test.env")
                .load();

        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysqlContainer.start();

        System.setProperty("DB_USERNAME", mysqlContainer.getUsername());
        System.setProperty("DB_PASSWORD", mysqlContainer.getPassword());
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION", "3600000"));
        System.setProperty("TELEGRAM_BOT_TOKEN", dotenv.get("TELEGRAM_BOT_TOKEN"));
        System.setProperty("TELEGRAM_BOT_USERNAME", dotenv.get("TELEGRAM_BOT_USERNAME"));
        System.setProperty("TELEGRAM_ADMIN_CHAT_ID", dotenv.get("TELEGRAM_ADMIN_CHAT_ID"));
        System.setProperty("TELEGRAM_BOT_ENABLED", "false");
        System.setProperty("TELEGRAM_REGISTER_ON_STARTUP", "false");
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
}
