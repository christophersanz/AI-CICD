package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;

/**
 * Inicia sesión con usuario y contraseña (actor: cualquiera). Si las credenciales son válidas y el usuario está
 * habilitado, emite un access token y un refresh token de una familia nueva.
 */
@FunctionalInterface
public interface AuthenticateUserUseCase {

    /**
     * Valida las credenciales y emite el par de tokens.
     *
     * @param command usuario y contraseña tal como los escribió la persona
     * @return el par de tokens (access token JWT y refresh token opaco) con sus expiraciones
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException si el usuario no
     *         existe, la contraseña no coincide o el usuario está deshabilitado: siempre el mismo error, para no
     *         revelar cuál fue (BR-USR-004, {@code AUTH-002})
     */
    TokenPairResult authenticate(LoginCommand command);
}
