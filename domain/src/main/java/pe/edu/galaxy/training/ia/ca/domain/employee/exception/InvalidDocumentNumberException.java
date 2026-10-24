package pe.edu.galaxy.training.ia.ca.domain.employee.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class InvalidDocumentNumberException extends DomainException {

    public InvalidDocumentNumberException(String message) {
        super("EMP-002", message);
    }
}
