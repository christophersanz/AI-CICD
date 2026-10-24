package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException;
import pe.edu.galaxy.training.ia.ca.application.port.out.CredentialsAuthenticatorPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final LoginCommand LOGIN = new LoginCommand("ana", "correct horse battery");
    private static final Set<Permission> PERMISSIONS = Set.of(new Permission("contract:create"));

    @Mock
    private CredentialsAuthenticatorPort credentialsAuthenticator;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private TokenIssuerPort tokenIssuer;
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    private AuthenticateUserService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticateUserService(credentialsAuthenticator, roleRepository, tokenIssuer,
                refreshTokenRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static User user(boolean enabled) {
        return User.restore(UserId.generate(), new Username("ana"), new PasswordHash("{bcrypt}x"),
                Set.of(RoleName.HR), enabled);
    }

    @Test
    void shouldIssueTokenPairAndPersistRefreshTokenHashWhenCredentialsAreValid() {
        // Given
        User user = user(true);
        when(credentialsAuthenticator.authenticate("ana", LOGIN.password())).thenReturn(user);
        when(roleRepository.findPermissionsByRoles(Set.of(RoleName.HR))).thenReturn(PERMISSIONS);
        when(tokenIssuer.issueAccessToken(user, PERMISSIONS))
                .thenReturn(new IssuedAccessToken("jwt", NOW.plus(Duration.ofMinutes(15))));
        when(tokenIssuer.generateRefreshToken())
                .thenReturn(new IssuedRefreshToken("opaque", "opaque-hash", NOW.plus(Duration.ofDays(7))));

        // When
        TokenPairResult result = service.authenticate(LOGIN);

        // Then
        assertThat(result.accessToken()).isEqualTo("jwt");
        assertThat(result.refreshToken()).isEqualTo("opaque");
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().tokenHash()).isEqualTo("opaque-hash");
        assertThat(saved.getValue().userId()).isEqualTo(user.id());
        assertThat(saved.getValue().issuedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPropagateInvalidCredentialsWhenAuthenticatorRejectsThem() {
        // Given
        when(credentialsAuthenticator.authenticate(any(), any())).thenThrow(new InvalidCredentialsException());

        // When / Then
        assertThatThrownBy(() -> service.authenticate(LOGIN))
                .isInstanceOf(InvalidCredentialsException.class)
                .extracting("code").isEqualTo("AUTH-002");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRejectLoginWithGenericErrorWhenUserIsDisabled() {
        // Given
        when(credentialsAuthenticator.authenticate(any(), any())).thenReturn(user(false));

        // When / Then
        assertThatThrownBy(() -> service.authenticate(LOGIN)).isInstanceOf(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issueAccessToken(any(), any());
    }

    @Test
    void shouldNotExposeTokensWhenResultIsPrinted() {
        TokenPairResult result = new TokenPairResult("jwt-secret", NOW, "refresh-secret", NOW);
        assertThat(result.toString()).doesNotContain("jwt-secret", "refresh-secret");
    }
}
