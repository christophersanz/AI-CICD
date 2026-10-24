package pe.edu.galaxy.training.ia.ca.domain.identity.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/** El refresh token no existe, expiró, fue revocado o reutilizado (BR-USR-006). */
public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super("AUTH-005", "Refresh token is not valid.");
    }
}
