package pe.edu.galaxy.training.ia.ca.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter.RefreshTokenPersistenceAdapter;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter.UserPersistenceAdapter;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.UserPersistenceMapper;

/** Refresh tokens contra PostgreSQL: rotación y revocación por familia. Se omite si Docker no está disponible. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import({RefreshTokenPersistenceAdapter.class, RefreshTokenPersistenceMapper.class,
        UserPersistenceAdapter.class, UserPersistenceMapper.class})
class RefreshTokenPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // Precisión de microsegundos, como TIMESTAMPTZ
    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final Instant EXPIRES = NOW.plus(Duration.ofDays(7));

    @Autowired
    private RefreshTokenPersistenceAdapter tokens;

    @Autowired
    private UserPersistenceAdapter users;

    @Autowired
    private EntityManager entityManager;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = users.save(User.create(new Username("owner"), new PasswordHash("{bcrypt}x"), Set.of(RoleName.HR)));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistIssuedTokenAndFindItByHash() {
        // Given
        RefreshToken token = RefreshToken.issue(owner.id(), "a".repeat(64), EXPIRES, CLOCK);

        // When
        tokens.save(token);
        flushAndClear();

        // Then
        RefreshToken loaded = tokens.findByTokenHashForUpdate("a".repeat(64)).orElseThrow();
        assertThat(loaded.id()).isEqualTo(token.id());
        assertThat(loaded.userId()).isEqualTo(owner.id());
        assertThat(loaded.familyId()).isEqualTo(token.familyId());
        assertThat(loaded.issuedAt()).isEqualTo(NOW);
        assertThat(loaded.expiresAt()).isEqualTo(EXPIRES);
        assertThat(loaded.isUsable(NOW)).isTrue();
        assertThat(tokens.findByTokenHashForUpdate("b".repeat(64))).isEmpty();
    }

    @Test
    void shouldPersistRotationWhenSuccessorIsSavedBeforeCurrent() {
        // Given
        RefreshToken current = tokens.save(RefreshToken.issue(owner.id(), "c".repeat(64), EXPIRES, CLOCK));
        flushAndClear();
        RefreshToken locked = tokens.findByTokenHashForUpdate("c".repeat(64)).orElseThrow();

        // When
        RefreshToken successor = locked.rotate("d".repeat(64), EXPIRES, CLOCK);
        tokens.save(successor);
        tokens.save(locked);
        flushAndClear();

        // Then
        RefreshToken reloaded = tokens.findByTokenHashForUpdate("c".repeat(64)).orElseThrow();
        assertThat(reloaded.isUsed()).isTrue();
        assertThat(reloaded.usedAt()).contains(NOW);
        assertThat(reloaded.replacedBy()).contains(successor.id());
        RefreshToken next = tokens.findByTokenHashForUpdate("d".repeat(64)).orElseThrow();
        assertThat(next.familyId()).isEqualTo(current.familyId());
        assertThat(next.isUsable(NOW)).isTrue();
    }

    @Test
    void shouldRevokeOnlyTokensOfTheGivenFamilyAndKeepEarlierReasons() {
        // Given: familia A con un token ya revocado y otro vigente; familia B vigente
        RefreshToken familyA = RefreshToken.issue(owner.id(), "e".repeat(64), EXPIRES, CLOCK);
        RefreshToken successorA = familyA.rotate("f".repeat(64), EXPIRES, CLOCK);
        familyA.revoke(RevocationReason.LOGOUT, CLOCK);
        tokens.save(successorA);
        tokens.save(familyA);
        tokens.save(RefreshToken.issue(owner.id(), "0".repeat(64), EXPIRES, CLOCK));
        flushAndClear();
        Instant revokedAt = NOW.plusSeconds(30);

        // When
        tokens.revokeFamily(familyA.familyId(), RevocationReason.REUSE_DETECTED, revokedAt);
        flushAndClear();

        // Then
        RefreshToken first = tokens.findByTokenHashForUpdate("e".repeat(64)).orElseThrow();
        assertThat(first.revocationReason()).contains(RevocationReason.LOGOUT);
        assertThat(first.revokedAt()).contains(NOW);
        RefreshToken second = tokens.findByTokenHashForUpdate("f".repeat(64)).orElseThrow();
        assertThat(second.revocationReason()).contains(RevocationReason.REUSE_DETECTED);
        assertThat(second.revokedAt()).contains(revokedAt);
        RefreshToken otherFamily = tokens.findByTokenHashForUpdate("0".repeat(64)).orElseThrow();
        assertThat(otherFamily.isRevoked()).isFalse();
    }
}
