package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
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

    static final String PROBLEM_DETAIL = "ProblemDetail";

    /**
     * springdoc describe las propiedades extra de {@code ProblemDetail} como un objeto {@code properties},
     * pero Jackson las escribe en el nivel superior. Se sustituye por los campos que devuelve la API:
     * {@code code} (catálogo de business-rules.md §8) y {@code errors} (solo en 400 {@code invalid-request}).
     */
    @Bean
    OpenApiCustomizer problemDetailSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            Schema<?> problem = openApi.getComponents().getSchemas().get(PROBLEM_DETAIL);
            if (problem == null || problem.getProperties() == null) {
                return;
            }
            problem.getProperties().remove("properties");
            problem.addProperty("code", new StringSchema()
                    .description("Código de error de negocio (business-rules.md §8)").example("AUTH-001"));
            problem.addProperty("errors", new ArraySchema()
                    .items(new ObjectSchema()
                            .addProperty("field", new StringSchema())
                            .addProperty("message", new StringSchema()))
                    .description("Campos inválidos; solo en 400 invalid-request"));
        };
    }
}
