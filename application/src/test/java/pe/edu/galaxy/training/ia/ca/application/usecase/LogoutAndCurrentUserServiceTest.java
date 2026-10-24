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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;
import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.UserNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

@ExtendWith(MockitoExtension.class)
class LogoutAndCurrentUserServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final UserId USER_ID = UserId.generate();

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private TokenIssuerPort tokenIssuer;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RoleRepositoryPort roleRepository;

    private LogoutService logoutService() {
        return new LogoutService(refreshTokenRepository, tokenIssuer, CLOCK);
    }

    private void givenStoredTokenOf(UserId owner, RefreshToken[] holder) {
        RefreshToken token = RefreshToken.issue(owner, "hash", NOW.plus(Duration.ofDays(7)), CLOCK);
        holder[0] = token;
        when(tokenIssuer.hashRefreshToken("presented")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("hash")).thenReturn(Optional.of(token));
    }

    @Test
    void shouldRevokeFamilyWhenTokenBelongsToAuthenticatedUser() {
        // Given
        RefreshToken[] token = new RefreshToken[1];
        givenStoredTokenOf(USER_ID, token);

        // When
        logoutService().logout(new LogoutCommand(USER_ID.value(), "presented"));

        // Then
        verify(refreshTokenRepository).revokeFamily(token[0].familyId(), RevocationReason.LOGOUT, NOW);
    }

    @Test
    void shouldIgnoreLogoutWhenTokenBelongsToAnotherUser() {
        // Given
        givenStoredTokenOf(UserId.generate(), new RefreshToken[1]);

        // When
        logoutService().logout(new LogoutCommand(USER_ID.value(), "presented"));

        // Then
        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
    }

    @Test
    void shouldIgnoreLogoutWhenTokenDoesNotExist() {
        // Given
        when(tokenIssuer.hashRefreshToken("unknown")).thenReturn("unknown-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("unknown-hash")).thenReturn(Optional.empty());

        // When
        logoutService().logout(new LogoutCommand(USER_ID.value(), "unknown"));

        // Then
        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
    }

    @Test
    void shouldReturnRolesAndEffectivePermissionsWhenCurrentUserExists() {
        // Given
        User user = User.restore(USER_ID, new Username("ana"), new PasswordHash("{bcrypt}x"),
                Set.of(RoleName.HR), true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findPermissionsByRoles(Set.of(RoleName.HR)))
                .thenReturn(Set.of(new Permission("contract:create")));

        // When
        CurrentUserResult result = new GetCurrentUserService(userRepository, roleRepository)
                .get(new CurrentUserQuery(USER_ID.value()));

        // Then
        assertThat(result.username()).isEqualTo("ana");
        assertThat(result.roles()).containsExactly("HR");
        assertThat(result.permissions()).containsExactly("contract:create");
    }

    @Test
    void shouldThrowUserNotFoundWhenCurrentUserNoLongerExists() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> new GetCurrentUserService(userRepository, roleRepository)
                .get(new CurrentUserQuery(USER_ID.value())))
                .isInstanceOf(UserNotFoundException.class)
                .extracting("code").isEqualTo("AUTH-404");
    }
}
