package car.sharing.service.chs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String API_TITLE = "Car Sharing API";
    private static final String API_VERSION = "1.0";

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String SECURITY_SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SECURITY_SCHEME)
                                        .bearerFormat(BEARER_FORMAT)
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                );
    }
}
