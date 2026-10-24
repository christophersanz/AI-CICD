package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final UserId USER_ID = UserId.generate();

    private static RefreshToken issued() {
        return RefreshToken.issue(USER_ID, "hash-1", NOW.plus(Duration.ofDays(7)), CLOCK);
    }

    @Test
    void shouldIssueUsableTokenInNewFamilyWhenUserLogsIn() {
        // When
        RefreshToken token = issued();

        // Then
        assertThat(token.isUsable(NOW)).isTrue();
        assertThat(token.issuedAt()).isEqualTo(NOW);
        assertThat(token.familyId()).isNotEqualTo(issued().familyId());
    }

    @Test
    void shouldMarkTokenUsedAndReturnSuccessorInSameFamilyWhenRotated() {
        // Given
        RefreshToken token = issued();

        // When
        RefreshToken successor = token.rotate("hash-2", NOW.plus(Duration.ofDays(7)), CLOCK);

        // Then
        assertThat(token.isUsed()).isTrue();
        assertThat(token.usedAt()).contains(NOW);
        assertThat(token.replacedBy()).contains(successor.id());
        assertThat(successor.familyId()).isEqualTo(token.familyId());
        assertThat(successor.userId()).isEqualTo(USER_ID);
        assertThat(successor.tokenHash()).isEqualTo("hash-2");
        assertThat(successor.isUsable(NOW)).isTrue();
    }

    @Test
    void shouldRejectRotationWhenTokenWasAlreadyUsed() {
        // Given
        RefreshToken token = issued();
        token.rotate("hash-2", NOW.plus(Duration.ofDays(7)), CLOCK);

        // When / Then
        assertThatThrownBy(() -> token.rotate("hash-3", NOW.plus(Duration.ofDays(7)), CLOCK))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .extracting("code").isEqualTo("AUTH-005");
    }

    @Test
    void shouldRejectRotationWhenTokenIsRevoked() {
        // Given
        RefreshToken token = issued();
        token.revoke(RevocationReason.LOGOUT, CLOCK);

        // When / Then
        assertThatThrownBy(() -> token.rotate("hash-2", NOW.plus(Duration.ofDays(7)), CLOCK))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldRejectRotationWhenTokenIsExpired() {
        // Given
        RefreshToken token = issued();
        Clock afterExpiration = Clock.fixed(NOW.plus(Duration.ofDays(7)), ZoneOffset.UTC);

        // When / Then
        assertThat(token.isUsable(afterExpiration.instant())).isFalse();
        assertThatThrownBy(() -> token.rotate("hash-2", NOW.plus(Duration.ofDays(14)), afterExpiration))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldKeepOriginalReasonWhenTokenIsRevokedTwice() {
        // Given
        RefreshToken token = issued();
        token.revoke(RevocationReason.REUSE_DETECTED, CLOCK);

        // When
        token.revoke(RevocationReason.LOGOUT, Clock.offset(CLOCK, Duration.ofMinutes(1)));

        // Then
        assertThat(token.revocationReason()).contains(RevocationReason.REUSE_DETECTED);
        assertThat(token.revokedAt()).contains(NOW);
    }

    @Test
    void shouldRejectTokenWhenExpirationIsNotAfterIssueInstant() {
        assertThatThrownBy(() -> RefreshToken.issue(USER_ID, "hash", NOW, CLOCK))
                .isInstanceOf(InvalidValueException.class);
    }
}
