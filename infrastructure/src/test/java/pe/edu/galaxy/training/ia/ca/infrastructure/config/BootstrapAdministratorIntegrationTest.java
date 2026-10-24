package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.AuthenticateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.BootstrapAdministratorUseCase;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt.JwtClaims;

/**
 * Contexto completo contra PostgreSQL (perfil por defecto {@code local}, clave efímera). Verifica el
 * wiring de los pasos 1.6–1.7 y BR-USR-007. Se omite si Docker no está disponible.
 */
@SpringBootTest(properties = {
        "security.bootstrap.admin.username=Root.Admin",
        "security.bootstrap.admin.password=" + BootstrapAdministratorIntegrationTest.PASSWORD
})
@Testcontainers(disabledWithoutDocker = true)
class BootstrapAdministratorIntegrationTest {

    static final String PASSWORD = "integration-admin-password";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private BootstrapAdministratorRunner runner;

    @Autowired
    private BootstrapAdministratorUseCase bootstrapAdministratorUseCase;

    @Autowired
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Autowired
    private JwtDecoder jwtDecoder;

    private int countAdmins() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM user_roles WHERE role_name = 'ADMIN'", Integer.class);
    }

    @Test
    void shouldCreateAdministratorWithHashedPasswordWhenContextStarts() {
        // Then
        assertThat(countAdmins()).isEqualTo(1);
        String hash = jdbc.queryForObject("SELECT password_hash FROM users WHERE username = 'root.admin'",
                String.class);
        assertThat(hash).startsWith("{bcrypt}").doesNotContain(PASSWORD);
    }

    @Test
    void shouldCreateAdministratorOnlyOnceWhenBootstrapRunsAgain() {
        // When
        runner.run(null);
        boolean created = bootstrapAdministratorUseCase.bootstrap(
                new BootstrapAdministratorCommand("another.admin", "another-admin-password"));

        // Then
        assertThat(created).isFalse();
        assertThat(countAdmins()).isEqualTo(1);
    }

    @Test
    void shouldIssueTokensAcceptedByDecoderWhenAdministratorLogsIn() {
        // When
        TokenPairResult tokens = authenticateUserUseCase.authenticate(new LoginCommand("ROOT.ADMIN", PASSWORD));

        // Then
        Jwt jwt = jwtDecoder.decode(tokens.accessToken());
        assertThat(jwt.getClaimAsString(JwtClaims.USERNAME)).isEqualTo("root.admin");
        assertThat(jwt.getClaimAsStringList(JwtClaims.PERMISSIONS)).contains("user:manage", "employee:create");
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM refresh_tokens", Integer.class)).isPositive();
    }
}
