package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

@ExtendWith(MockitoExtension.class)
class BootstrapAdministratorServiceTest {

    private static final BootstrapAdministratorCommand COMMAND =
            new BootstrapAdministratorCommand("admin", "initial admin password");

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;

    @InjectMocks
    private BootstrapAdministratorService service;

    @Test
    void shouldCreateAdministratorWhenNoAdminExists() {
        // Given
        when(userRepository.existsByRole(RoleName.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername(new Username("admin"))).thenReturn(false);
        when(passwordHasher.hash(COMMAND.password())).thenReturn(new PasswordHash("{bcrypt}hashed"));

        // When
        boolean created = service.bootstrap(COMMAND);

        // Then
        assertThat(created).isTrue();
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().roles()).containsExactly(RoleName.ADMIN);
        assertThat(saved.getValue().isEnabled()).isTrue();
    }

    @Test
    void shouldDoNothingWhenAnAdministratorAlreadyExists() {
        // Given
        when(userRepository.existsByRole(RoleName.ADMIN)).thenReturn(true);

        // When
        boolean created = service.bootstrap(COMMAND);

        // Then
        assertThat(created).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateUsernameWhenUsernameBelongsToNonAdminUser() {
        // Given
        when(userRepository.existsByRole(RoleName.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername(new Username("admin"))).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.bootstrap(COMMAND)).isInstanceOf(DuplicateUsernameException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBootstrapWhenPasswordViolatesPolicy() {
        // Given
        when(userRepository.existsByRole(RoleName.ADMIN)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.bootstrap(new BootstrapAdministratorCommand("admin", "admin")))
                .isInstanceOf(WeakPasswordException.class);
    }
}
