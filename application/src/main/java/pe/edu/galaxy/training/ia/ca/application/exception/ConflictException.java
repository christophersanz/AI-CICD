package pe.edu.galaxy.training.ia.ca.application.exception;

/** El estado actual impide la operación, por ejemplo un dato único ya ocupado. La API responde 409. */
public abstract class ConflictException extends ApplicationException {

    /**
     * Crea la excepción con su código y su detalle técnico.
     *
     * @param code código de error del catálogo
     * @param message detalle técnico en inglés
     */
    protected ConflictException(String code, String message) {
        super(code, message);
    }
}
