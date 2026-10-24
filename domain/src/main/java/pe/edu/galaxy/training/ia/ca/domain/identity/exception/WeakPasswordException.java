package pe.edu.galaxy.training.ia.ca.domain.identity.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class WeakPasswordException extends DomainException {

    public WeakPasswordException(String message) {
        super("AUTH-008", message);
    }
}
