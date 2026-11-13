package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/** Un valor no cumple el formato requerido por el dominio (VAL-001). */
public class InvalidValueException extends DomainException {

    /**
     * Crea el error con el código {@code VAL-001}.
     *
     * @param message qué valor falta o no cumple su formato
     */
    public InvalidValueException(String message) {
        super("VAL-001", message);
    }
}
