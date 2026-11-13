package pe.edu.galaxy.training.ia.ca.domain.employee.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * El documento de identidad no cumple el formato de su tipo, por ejemplo un DNI sin 8 dígitos (BR-EMP-002,
 * {@code EMP-002}, HTTP 422).
 */
public class InvalidDocumentNumberException extends DomainException {

    /**
     * Crea el error con el código {@code EMP-002}.
     *
     * @param message qué regla de formato no se cumple
     */
    public InvalidDocumentNumberException(String message) {
        super("EMP-002", message);
    }
}
