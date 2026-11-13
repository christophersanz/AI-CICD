package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;

/** Persistencia de los refresh tokens. Solo se guarda el hash del token, nunca su valor (BR-USR-006). */
public interface RefreshTokenRepositoryPort {

    /**
     * Busca por hash bloqueando la fila hasta el fin de la transacción, para que dos rotaciones simultáneas del mismo
     * token no emitan dos pares (BR-USR-006).
     *
     * @param tokenHash hash del token presentado, calculado con {@link TokenIssuerPort#hashRefreshToken(String)}
     * @return el token, o vacío si no existe
     */
    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

    /**
     * Inserta el token o actualiza su estado (usado, revocado).
     * @param refreshToken el token a guardar
     * @return el token tal como quedó guardado
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Revoca los tokens no revocados de la familia, en logout o ante reutilización (BR-USR-006). Los ya revocados
     * conservan su motivo y fecha originales.
     *
     * @param familyId familia que nació en un mismo login
     * @param reason motivo que queda registrado en cada token
     * @param revokedAt instante de la revocación
     */
    void revokeFamily(UUID familyId, RevocationReason reason, Instant revokedAt);
}
