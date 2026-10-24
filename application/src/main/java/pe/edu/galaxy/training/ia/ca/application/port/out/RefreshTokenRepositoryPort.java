package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;

public interface RefreshTokenRepositoryPort {

    /** Busca por hash bloqueando la fila hasta el fin de la transacción, para serializar rotaciones. */
    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    /** Revoca los tokens no revocados de la familia. */
    void revokeFamily(UUID familyId, RevocationReason reason, Instant revokedAt);
}
