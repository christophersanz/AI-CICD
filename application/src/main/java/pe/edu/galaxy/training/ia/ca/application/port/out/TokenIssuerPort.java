package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Set;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;

/** Emite access tokens firmados y genera refresh tokens opacos. Las vidas útiles son configuración. */
public interface TokenIssuerPort {

    IssuedAccessToken issueAccessToken(User user, Set<Permission> permissions);

    IssuedRefreshToken generateRefreshToken();

    /** Mismo hash que {@link IssuedRefreshToken#hash()}, para buscar un token presentado por el cliente. */
    String hashRefreshToken(String rawRefreshToken);
}
