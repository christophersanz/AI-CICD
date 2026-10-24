package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/** Un valor no cumple el formato requerido por el dominio (VAL-001). */
public class InvalidValueException extends DomainException {

    public InvalidValueException(String message) {
        super("VAL-001", message);
    }
}
