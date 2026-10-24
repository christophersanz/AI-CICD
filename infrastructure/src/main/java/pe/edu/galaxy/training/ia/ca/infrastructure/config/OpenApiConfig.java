package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Esquema {@code bearerAuth} de OpenAPI: permite probar desde Swagger UI con el botón <i>Authorize</i>
 * pegando el {@code accessToken} de {@code POST /api/v1/auth/login}. Se aplica con
 * {@code @SecurityRequirement} en los endpoints privados.
 */
@Configuration
@SecurityScheme(name = OpenApiConfig.BEARER_AUTH, type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "JWT", description = "Access token obtenido en POST /api/v1/auth/login")
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
