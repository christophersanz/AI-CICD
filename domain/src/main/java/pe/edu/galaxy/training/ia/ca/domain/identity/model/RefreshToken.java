package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.AggregateRoot;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RefreshTokenId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

/**
 * Refresh token de un solo uso (BR-USR-006). Solo se guarda el hash del valor entregado al cliente.
 * Los tokens nacidos de un mismo login comparten {@code familyId}.
 */
public final class RefreshToken extends AggregateRoot<RefreshTokenId> {

    private final UserId userId;
    private final UUID familyId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant usedAt;
    private RefreshTokenId replacedBy;
    private Instant revokedAt;
    private RevocationReason revocationReason;

    private RefreshToken(RefreshTokenId id, UserId userId, UUID familyId, String tokenHash, Instant issuedAt,
                         Instant expiresAt, Instant usedAt, RefreshTokenId replacedBy, Instant revokedAt,
                         RevocationReason revocationReason) {
        super(id);
        this.userId = Guard.notNull(userId, "userId");
        this.familyId = Guard.notNull(familyId, "familyId");
        this.tokenHash = Guard.notNull(tokenHash, "tokenHash");
        this.issuedAt = Guard.notNull(issuedAt, "issuedAt");
        this.expiresAt = Guard.notNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new InvalidValueException("Refresh token expiration must be after its issue instant.");
        }
        this.usedAt = usedAt;
        this.replacedBy = replacedBy;
        this.revokedAt = revokedAt;
        this.revocationReason = revocationReason;
    }

    /** Emite el primer token de una familia nueva (login). */
    public static RefreshToken issue(UserId userId, String tokenHash, Instant expiresAt, Clock clock) {
        return new RefreshToken(RefreshTokenId.generate(), userId, UUID.randomUUID(), tokenHash, clock.instant(),
                expiresAt, null, null, null, null);
    }

    /** {@code usedAt}, {@code replacedBy}, {@code revokedAt} y {@code revocationReason} son {@code null} si no aplican. */
    public static RefreshToken restore(RefreshTokenId id, UserId userId, UUID familyId, String tokenHash,
                                       Instant issuedAt, Instant expiresAt, Instant usedAt,
                                       RefreshTokenId replacedBy, Instant revokedAt,
                                       RevocationReason revocationReason) {
        return new RefreshToken(id, userId, familyId, tokenHash, issuedAt, expiresAt, usedAt, replacedBy,
                revokedAt, revocationReason);
    }

    /**
     * Marca este token como usado y devuelve su sucesor en la misma familia.
     *
     * @throws InvalidRefreshTokenException si ya fue usado, está revocado o expiró
     */
    public RefreshToken rotate(String newTokenHash, Instant newExpiresAt, Clock clock) {
        Instant now = clock.instant();
        if (!isUsable(now)) {
            throw new InvalidRefreshTokenException();
        }
        RefreshToken successor = new RefreshToken(RefreshTokenId.generate(), userId, familyId, newTokenHash, now,
                newExpiresAt, null, null, null, null);
        usedAt = now;
        replacedBy = successor.id();
        return successor;
    }

    /** Revoca el token; si ya estaba revocado conserva el motivo original. */
    public void revoke(RevocationReason reason, Clock clock) {
        Guard.notNull(reason, "reason");
        if (revokedAt == null) {
            revokedAt = clock.instant();
            revocationReason = reason;
        }
    }

    public boolean isUsable(Instant now) {
        return !isUsed() && !isRevoked() && now.isBefore(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public UserId userId() {
        return userId;
    }

    public UUID familyId() {
        return familyId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Optional<Instant> usedAt() {
        return Optional.ofNullable(usedAt);
    }

    public Optional<RefreshTokenId> replacedBy() {
        return Optional.ofNullable(replacedBy);
    }

    public Optional<Instant> revokedAt() {
        return Optional.ofNullable(revokedAt);
    }

    public Optional<RevocationReason> revocationReason() {
        return Optional.ofNullable(revocationReason);
    }
}
