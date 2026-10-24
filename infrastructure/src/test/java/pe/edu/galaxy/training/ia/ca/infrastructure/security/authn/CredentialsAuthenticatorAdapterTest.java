package pe.edu.galaxy.training.ia.ca.infrastructure.security.authn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto.PasswordEncoderConfig;

/** {@code DaoAuthenticationProvider} real con el encoder de la aplicación; solo se simula el repositorio. */
class CredentialsAuthenticatorAdapterTest {

    private static final String PASSWORD = "correct horse battery";
    private static final PasswordEncoder ENCODER =
            PasswordEncoderConfig.create(new SecurityProperties.Password("bcrypt", 5));

    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private CredentialsAuthenticatorAdapter adapter;

    @BeforeEach
    void setUp() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        adapter = new CredentialsAuthenticatorAdapter(
                AuthenticationConfig.create(new DbUserDetailsService(userRepository), ENCODER));
    }

    private User givenUser(String username, String hash, boolean enabled) {
        User user = User.restore(UserId.generate(), new Username(username), new PasswordHash(hash),
                Set.of(RoleName.HR), enabled);
        when(userRepository.findByUsername(new Username(username))).thenReturn(Optional.of(user));
        return user;
    }

    private static String hashWith(String algorithm, int bcryptStrength) {
        return PasswordEncoderConfig.create(new SecurityProperties.Password(algorithm, bcryptStrength)).encode(PASSWORD);
    }

    @Test
    void shouldReturnDomainUserWhenCredentialsAreValid() {
        // Given
        User user = givenUser("ana", ENCODER.encode(PASSWORD), true);

        // When
        User authenticated = adapter.authenticate("ana", PASSWORD);

        // Then
        assertThat(authenticated.id()).isEqualTo(user.id());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNormalizeUsernameWhenItHasUppercaseLetters() {
        // Given
        User user = givenUser("ana", ENCODER.encode(PASSWORD), true);

        // When / Then
        assertThat(adapter.authenticate("ANA", PASSWORD).id()).isEqualTo(user.id());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordIsWrong() {
        // Given
        givenUser("ana", ENCODER.encode(PASSWORD), true);

        // When / Then
        assertThatThrownBy(() -> adapter.authenticate("ana", "wrong password value"))
                .isInstanceOf(InvalidCredentialsException.class)
                .extracting("code").isEqualTo("AUTH-002");
    }

    @Test
    void shouldThrowInvalidCredentialsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> adapter.authenticate("ghost", PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowInvalidCredentialsWithoutQueryingWhenUsernameFormatIsInvalid() {
        assertThatThrownBy(() -> adapter.authenticate("a b", PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenUserIsDisabled() {
        // Given
        givenUser("ana", ENCODER.encode(PASSWORD), false);

        // When / Then: mismo error que una contraseña incorrecta (BR-USR-004)
        assertThatThrownBy(() -> adapter.authenticate("ana", PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowInvalidCredentialsWhenDisabledUserGivesWrongPassword() {
        // Given
        givenUser("ana", ENCODER.encode(PASSWORD), false);

        // When / Then
        assertThatThrownBy(() -> adapter.authenticate("ana", "wrong password value"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRehashAndPersistWhenBcryptStrengthIsLowerThanConfigured() {
        // Given
        givenUser("ana", hashWith("bcrypt", 4), true);

        // When
        User authenticated = adapter.authenticate("ana", PASSWORD);

        // Then
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        String newHash = saved.getValue().passwordHash().value();
        assertThat(newHash).startsWith("{bcrypt}$2a$05$");
        assertThat(ENCODER.matches(PASSWORD, newHash)).isTrue();
        assertThat(authenticated.passwordHash().value()).isEqualTo(newHash);
    }

    @Test
    void shouldRehashToDefaultAlgorithmWhenHashUsesAnotherPrefix() {
        // Given
        givenUser("ana", hashWith("pbkdf2", 5), true);

        // When
        adapter.authenticate("ana", PASSWORD);

        // Then
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().passwordHash().value()).startsWith("{bcrypt}");
        assertThat(ENCODER.matches(PASSWORD, saved.getValue().passwordHash().value())).isTrue();
    }

    @Test
    void shouldNotRehashWhenPasswordIsWrong() {
        // Given
        givenUser("ana", hashWith("bcrypt", 4), true);

        // When / Then
        assertThatThrownBy(() -> adapter.authenticate("ana", "wrong password value"))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldMatchTruncatedInputWhenBcryptComparesMoreThan72Bytes() {
        // Documenta el motivo del rechazo previo del adaptador: BCrypt solo compara los primeros 72 bytes
        String password72 = "p".repeat(72);
        String hash = ENCODER.encode(password72);

        assertThat(ENCODER.matches(password72 + "-any-suffix", hash)).isTrue();
    }

    @Test
    void shouldThrowInvalidCredentialsWhenInputExtendsA72BytePassword() {
        // Given
        String password72 = "p".repeat(72);
        givenUser("ana", ENCODER.encode(password72), true);

        // When / Then
        assertThat(adapter.authenticate("ana", password72)).isNotNull();
        assertThatThrownBy(() -> adapter.authenticate("ana", password72 + "-any-suffix"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordExceeds72Bytes() {
        // Given: 37 caracteres "ñ" = 74 bytes en UTF-8
        givenUser("ana", ENCODER.encode(PASSWORD), true);

        // When / Then
        assertThatThrownBy(() -> adapter.authenticate("ana", "ñ".repeat(37)))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> adapter.authenticate("ghost", "x".repeat(100)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    static Stream<AuthenticationException> credentialFailures() {
        return Stream.of(new BadCredentialsException("bad"), new UsernameNotFoundException("none"),
                new DisabledException("disabled"), new LockedException("locked"));
    }

    @ParameterizedTest
    @MethodSource("credentialFailures")
    void shouldTranslateToInvalidCredentialsWhenAuthenticationFails(AuthenticationException failure) {
        // Given
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(manager.authenticate(any())).thenThrow(failure);

        // When / Then
        assertThatThrownBy(() -> new CredentialsAuthenticatorAdapter(manager).authenticate("ana", PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldNotMaskTechnicalFailureAsInvalidCredentialsWhenRepositoryFails() {
        // Given
        when(userRepository.findByUsername(any())).thenThrow(new IllegalStateException("database down"));

        // When / Then
        assertThatThrownBy(() -> adapter.authenticate("ana", PASSWORD))
                .isInstanceOf(InternalAuthenticationServiceException.class);
    }
}
