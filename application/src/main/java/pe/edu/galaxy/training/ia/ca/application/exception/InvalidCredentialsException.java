package pe.edu.galaxy.training.ia.ca.application.exception;

/**
 * Credenciales inválidas. También se usa para usuarios deshabilitados (BR-USR-004),
 * para no revelar qué cuentas existen ni su estado.
 */
public class InvalidCredentialsException extends UnauthenticatedException {

    public InvalidCredentialsException() {
        super("AUTH-002", "Invalid username or password.");
    }
}
