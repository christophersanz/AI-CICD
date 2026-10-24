package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final RefreshTokenCommand COMMAND = new RefreshTokenCommand("presented");
    private static final UserId USER_ID = UserId.generate();

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private TokenIssuerPort tokenIssuer;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, userRepository, roleRepository, tokenIssuer, CLOCK);
    }

    private static RefreshToken storedToken() {
        Clock issuedClock = Clock.offset(CLOCK, Duration.ofHours(-1));
        return RefreshToken.issue(USER_ID, "presented-hash", NOW.plus(Duration.ofDays(6)), issuedClock);
    }

    private static User user(boolean enabled) {
        return User.restore(USER_ID, new Username("ana"), new PasswordHash("{bcrypt}x"), Set.of(RoleName.HR), enabled);
    }

    private void givenStored(RefreshToken token) {
        when(tokenIssuer.hashRefreshToken("presented")).thenReturn("presented-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("presented-hash")).thenReturn(Optional.of(token));
    }

    @Test
    void shouldRotateTokenAndIssueNewPairWhenTokenIsValid() {
        // Given
        RefreshToken current = storedToken();
        givenStored(current);
        User user = user(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tokenIssuer.generateRefreshToken())
                .thenReturn(new IssuedRefreshToken("next", "next-hash", NOW.plus(Duration.ofDays(7))));
        when(roleRepository.findPermissionsByRoles(any())).thenReturn(Set.of());
        when(tokenIssuer.issueAccessToken(user, Set.of()))
                .thenReturn(new IssuedAccessToken("jwt", NOW.plus(Duration.ofMinutes(15))));

        // When
        TokenPairResult result = service.refresh(COMMAND);

        // Then
        assertThat(result.accessToken()).isEqualTo("jwt");
        assertThat(result.refreshToken()).isEqualTo("next");
        assertThat(current.isUsed()).isTrue();
        // El sucesor se guarda antes que el actual, que lo referencia en replacedBy
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(saved.capture());
        RefreshToken successor = saved.getAllValues().get(0);
        assertThat(saved.getAllValues().get(1)).isSameAs(current);
        assertThat(successor.tokenHash()).isEqualTo("next-hash");
        assertThat(successor.familyId()).isEqualTo(current.familyId());
        assertThat(current.replacedBy()).contains(successor.id());
    }

    @Test
    void shouldRevokeWholeFamilyWhenAlreadyUsedTokenIsPresented() {
        // Given
        RefreshToken current = storedToken();
        current.rotate("other-hash", NOW.plus(Duration.ofDays(7)), CLOCK);
        givenStored(current);

        // When / Then
        assertThatThrownBy(() -> service.refresh(COMMAND))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .extracting("code").isEqualTo("AUTH-005");
        verify(refreshTokenRepository).revokeFamily(current.familyId(), RevocationReason.REUSE_DETECTED, NOW);
        verify(tokenIssuer, never()).issueAccessToken(any(), any());
    }

    @Test
    void shouldRejectWithoutRevokingFamilyWhenTokenIsExpired() {
        // Given
        Clock longAgo = Clock.offset(CLOCK, Duration.ofDays(-8));
        RefreshToken expired = RefreshToken.issue(USER_ID, "presented-hash", NOW.minus(Duration.ofDays(1)), longAgo);
        givenStored(expired);

        // When / Then
        assertThatThrownBy(() -> service.refresh(COMMAND)).isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
    }

    @Test
    void shouldRejectWhenTokenIsRevoked() {
        // Given
        RefreshToken revoked = storedToken();
        revoked.revoke(RevocationReason.LOGOUT, CLOCK);
        givenStored(revoked);

        // When / Then
        assertThatThrownBy(() -> service.refresh(COMMAND)).isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenTokenDoesNotExist() {
        // Given
        when(tokenIssuer.hashRefreshToken("presented")).thenReturn("presented-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate("presented-hash")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.refresh(COMMAND)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldRevokeFamilyWhenUserIsDisabled() {
        // Given
        RefreshToken current = storedToken();
        givenStored(current);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(false)));

        // When / Then
        assertThatThrownBy(() -> service.refresh(COMMAND)).isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository).revokeFamily(current.familyId(), RevocationReason.USER_DISABLED, NOW);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldKeepRevocationCommittedWhenRefreshFails() {
        Transactional transactional = RefreshTokenService.class.getAnnotation(Transactional.class);
        assertThat(transactional.noRollbackFor()).contains(InvalidRefreshTokenException.class);
    }
}
