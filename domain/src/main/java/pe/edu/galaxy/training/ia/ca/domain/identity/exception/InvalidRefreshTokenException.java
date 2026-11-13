package pe.edu.galaxy.training.ia.ca.domain.identity.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/** El refresh token no existe, expiró, fue revocado o reutilizado (BR-USR-006). */
public class InvalidRefreshTokenException extends DomainException {

    /** Crea el error sin datos del token, para no revelar por qué se rechazó. */
    public InvalidRefreshTokenException() {
        super("AUTH-005", "Refresh token is not valid.");
    }
}
