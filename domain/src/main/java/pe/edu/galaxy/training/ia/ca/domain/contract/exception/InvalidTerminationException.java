package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class InvalidTerminationException extends DomainException {

    public InvalidTerminationException(String message) {
        super("CTR-007", message);
    }
}
