package pe.edu.galaxy.training.ia.ca.application.port.out;

import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;

/** Verifica usuario y contraseña. Si el hash está desactualizado, el adaptador lo actualiza. */
@FunctionalInterface
public interface CredentialsAuthenticatorPort {

    /**
     * Comprueba la contraseña contra el hash guardado y devuelve el usuario autenticado.
     *
     * @param username nombre de usuario tal como lo escribió la persona; el adaptador lo normaliza
     * @param password contraseña en claro; nunca se registra
     * @return el usuario habilitado cuyas credenciales coinciden
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException si el usuario no
     *         existe, la contraseña no coincide o el usuario está deshabilitado: siempre el mismo error (BR-USR-004,
     *         {@code AUTH-002})
     */
    User authenticate(String username, String password);
}
