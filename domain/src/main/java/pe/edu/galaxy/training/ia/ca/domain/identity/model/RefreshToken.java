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

    /**
     * Emite el primer token de una familia nueva, al iniciar sesión.
     *
     * @param userId dueño del token
     * @param tokenHash hash del valor que recibe el cliente; el valor nunca se guarda
     * @param expiresAt expiración, posterior al instante de emisión
     * @param clock reloj del que sale el instante de emisión
     * @return el token nuevo, sin usar ni revocar
     * @throws InvalidValueException si falta un dato o la expiración no es posterior a la emisión ({@code VAL-001})
     */
    public static RefreshToken issue(UserId userId, String tokenHash, Instant expiresAt, Clock clock) {
        return new RefreshToken(RefreshTokenId.generate(), userId, UUID.randomUUID(), tokenHash, clock.instant(),
                expiresAt, null, null, null, null);
    }

    /**
     * Reconstruye un token ya persistido. Uso exclusivo de los adaptadores de persistencia.
     *
     * @param id identificador persistido
     * @param userId dueño del token
     * @param familyId familia a la que pertenece
     * @param tokenHash hash del valor entregado al cliente
     * @param issuedAt instante de emisión
     * @param expiresAt expiración
     * @param usedAt instante en que se usó, o {@code null} si no se usó
     * @param replacedBy sucesor emitido al usarlo, o {@code null}
     * @param revokedAt instante de la revocación, o {@code null}
     * @param revocationReason motivo de la revocación, o {@code null}
     * @return el token reconstruido
     */
    public static RefreshToken restore(RefreshTokenId id, UserId userId, UUID familyId, String tokenHash,
                                       Instant issuedAt, Instant expiresAt, Instant usedAt,
                                       RefreshTokenId replacedBy, Instant revokedAt,
                                       RevocationReason revocationReason) {
        return new RefreshToken(id, userId, familyId, tokenHash, issuedAt, expiresAt, usedAt, replacedBy,
                revokedAt, revocationReason);
    }

    /**
     * Marca este token como usado y devuelve su sucesor en la misma familia (BR-USR-006). Si el token ya se usó, quien
     * llama debe revocar la familia: es una reutilización.
     *
     * @param newTokenHash hash del valor del sucesor
     * @param newExpiresAt expiración del sucesor
     * @param clock reloj del instante de uso, que es también la emisión del sucesor
     * @return el sucesor, sin usar
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

    /**
     * Revoca el token; si ya estaba revocado conserva el motivo y la fecha originales.
     *
     * @param reason motivo de la revocación
     * @param clock reloj del instante de revocación
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta el motivo ({@code
     *         VAL-001})
     */
    public void revoke(RevocationReason reason, Clock clock) {
        Guard.notNull(reason, "reason");
        if (revokedAt == null) {
            revokedAt = clock.instant();
            revocationReason = reason;
        }
    }

    /**
     * Indica si el token puede rotarse: no se usó, no está revocado y no expiró.
     * @param now instante de referencia
     * @return {@code true} si se puede usar en ese instante
     */
    public boolean isUsable(Instant now) {
        return !isUsed() && !isRevoked() && now.isBefore(expiresAt);
    }

    /** {@return {@code true} si ya se usó para rotar} */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** {@return {@code true} si fue revocado} */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** {@return el dueño del token} */
    public UserId userId() {
        return userId;
    }

    /** {@return la familia del token, compartida por los tokens de un mismo login} */
    public UUID familyId() {
        return familyId;
    }

    /** {@return el hash del valor entregado al cliente} */
    public String tokenHash() {
        return tokenHash;
    }

    /** {@return el instante de emisión} */
    public Instant issuedAt() {
        return issuedAt;
    }

    /** {@return el instante de expiración} */
    public Instant expiresAt() {
        return expiresAt;
    }

    /** {@return el instante en que se usó, o vacío si no se usó} */
    public Optional<Instant> usedAt() {
        return Optional.ofNullable(usedAt);
    }

    /** {@return el sucesor emitido al usarlo, o vacío} */
    public Optional<RefreshTokenId> replacedBy() {
        return Optional.ofNullable(replacedBy);
    }

    /** {@return el instante de la revocación, o vacío} */
    public Optional<Instant> revokedAt() {
        return Optional.ofNullable(revokedAt);
    }

    /** {@return el motivo de la revocación, o vacío} */
    public Optional<RevocationReason> revocationReason() {
        return Optional.ofNullable(revocationReason);
    }
}
