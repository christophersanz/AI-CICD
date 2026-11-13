package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;

/**
 * Rota el refresh token (actor: quien presente un refresh token): marca el actual como usado y emite un par nuevo de
 * la misma familia. Si el token ya se usó o el usuario está deshabilitado, revoca toda la familia (BR-USR-004,
 * BR-USR-006).
 */
@FunctionalInterface
public interface RefreshTokenUseCase {

    /**
     * Consume el refresh token y emite el siguiente de su familia.
     *
     * @param command el refresh token opaco recibido en el login o en la rotación anterior
     * @return el par nuevo de tokens
     * @throws pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException si el token no
     *         existe, expiró, fue revocado o reutilizado, o si el usuario ya no existe o está deshabilitado ({@code
     *         AUTH-005})
     */
    TokenPairResult refresh(RefreshTokenCommand command);
}
