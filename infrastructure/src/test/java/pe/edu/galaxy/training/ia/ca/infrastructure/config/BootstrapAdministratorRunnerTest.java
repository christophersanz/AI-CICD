package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.port.in.BootstrapAdministratorUseCase;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

class BootstrapAdministratorRunnerTest {

    private static final String PASSWORD = "a-strong-admin-password";

    private final BootstrapAdministratorUseCase useCase = mock(BootstrapAdministratorUseCase.class);

    private BootstrapAdministratorRunner runner(String username, String password) {
        SecurityProperties properties = new SecurityProperties(null, null,
                new SecurityProperties.Bootstrap(new SecurityProperties.Admin(username, password)));
        return new BootstrapAdministratorRunner(useCase, properties);
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL, NULL", "root, NULL", "NULL, secret-password", "' ', secret-password"},
            nullValues = "NULL")
    void shouldNotBootstrapWhenVariablesAreMissing(String username, String password) {
        // When
        runner(username, password).run(null);

        // Then
        verifyNoInteractions(useCase);
    }

    @Test
    void shouldBootstrapWithConfiguredCredentialsWhenVariablesExist() {
        // Given
        when(useCase.bootstrap(any())).thenReturn(true);

        // When
        runner("root", PASSWORD).run(null);

        // Then
        verify(useCase).bootstrap(new BootstrapAdministratorCommand("root", PASSWORD));
    }

    @Test
    void shouldFailStartupWithoutPasswordInMessageWhenPasswordIsWeak() {
        // Given
        when(useCase.bootstrap(any())).thenThrow(new WeakPasswordException("Password must have between 12 and 64 characters."));

        // When / Then
        assertThatThrownBy(() -> runner("root", "short").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AUTH-008")
                .hasMessageNotContaining("short");
    }

    @Test
    void shouldFailStartupWhenUsernameBelongsToAnotherUser() {
        // Given
        when(useCase.bootstrap(any())).thenThrow(new DuplicateUsernameException(new Username("root")));

        // When / Then
        assertThatThrownBy(() -> runner("root", PASSWORD).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AUTH-007")
                .hasMessageNotContaining(PASSWORD);
    }

    @Test
    void shouldNotExposePasswordWhenPropertiesArePrinted() {
        assertThat(new SecurityProperties.Admin("root", PASSWORD).toString()).doesNotContain(PASSWORD);
    }
}
