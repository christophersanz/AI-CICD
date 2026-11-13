package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Set;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;

/** Emite access tokens firmados y genera refresh tokens opacos. Las vidas útiles son configuración. */
public interface TokenIssuerPort {

    /**
     * Firma un access token JWT para el usuario.
     * @param user usuario autenticado; su identificador va en {@code sub}
     * @param permissions permisos efectivos que viajan en el token
     * @return el token firmado y su expiración
     */
    IssuedAccessToken issueAccessToken(User user, Set<Permission> permissions);

    /**
     * Genera un refresh token opaco y aleatorio.
     * @return el valor que recibe el cliente, su hash para guardarlo y su expiración
     */
    IssuedRefreshToken generateRefreshToken();

    /**
     * Calcula el hash de un refresh token presentado por el cliente, con el mismo algoritmo que
     * {@link IssuedRefreshToken#hash()}, para poder buscarlo.
     *
     * @param rawRefreshToken valor opaco recibido del cliente
     * @return el hash con el que se guardó
     */
    String hashRefreshToken(String rawRefreshToken);
}
