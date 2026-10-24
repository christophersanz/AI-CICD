package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.exception.RoleNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    private static final String PASSWORD = "correct horse battery";

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private PasswordHasherPort passwordHasher;

    @InjectMocks
    private CreateUserService service;

    @Test
    void shouldCreateEnabledUserWithHashedPasswordWhenDataIsValid() {
        // Given
        when(userRepository.existsByUsername(new Username("ana"))).thenReturn(false);
        when(roleRepository.findAllByNames(Set.of(RoleName.HR))).thenReturn(List.of(new Role(RoleName.HR, Set.of())));
        when(passwordHasher.hash(PASSWORD)).thenReturn(new PasswordHash("{bcrypt}hashed"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResult result = service.create(new CreateUserCommand("Ana", PASSWORD, Set.of("hr")));

        // Then
        assertThat(result.username()).isEqualTo("ana");
        assertThat(result.roles()).containsExactly("HR");
        assertThat(result.enabled()).isTrue();
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().passwordHash().value()).isEqualTo("{bcrypt}hashed");
    }

    @Test
    void shouldThrowDuplicateUsernameWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(new Username("ana"))).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(new CreateUserCommand("ANA", PASSWORD, Set.of("HR"))))
                .isInstanceOf(DuplicateUsernameException.class)
                .extracting("code").isEqualTo("AUTH-007");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowRoleNotFoundWhenRoleDoesNotExist() {
        // Given
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(roleRepository.findAllByNames(any())).thenReturn(List.of(new Role(RoleName.HR, Set.of())));

        // When / Then
        assertThatThrownBy(() -> service.create(new CreateUserCommand("ana", PASSWORD, Set.of("HR", "AUDITOR"))))
                .isInstanceOf(RoleNotFoundException.class)
                .extracting("code").isEqualTo("AUTH-009");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectUserWhenPasswordViolatesPolicy() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand("ana", "short", Set.of("HR"))))
                .isInstanceOf(WeakPasswordException.class);
        verifyNoInteractions(userRepository, roleRepository, passwordHasher);
    }

    @Test
    void shouldRejectUserWhenUsernameIsInvalid() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand("a b", PASSWORD, Set.of("HR"))))
                .isInstanceOf(InvalidValueException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldNotExposePasswordWhenCommandIsPrinted() {
        assertThat(new CreateUserCommand("ana", PASSWORD, Set.of()).toString()).doesNotContain(PASSWORD);
    }
}
