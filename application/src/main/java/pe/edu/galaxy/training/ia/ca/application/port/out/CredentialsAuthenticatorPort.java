package pe.edu.galaxy.training.ia.ca.application.port.out;

import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;

/** Verifica usuario y contraseña. Si el hash está desactualizado, el adaptador lo actualiza. */
@FunctionalInterface
public interface CredentialsAuthenticatorPort {

    /**
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException
     *         si el usuario no existe, la contraseña no coincide o el usuario está deshabilitado
     */
    User authenticate(String username, String password);
}
