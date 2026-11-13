package pe.edu.galaxy.training.ia.ca.application.exception;

/** La identidad del solicitante no pudo verificarse. */
public abstract class UnauthenticatedException extends ApplicationException {

    /**
     * Crea la excepción con su código y su detalle técnico.
     *
     * @param code código de error del catálogo
     * @param message detalle técnico en inglés; nunca dice cuál fue el motivo real
     */
    protected UnauthenticatedException(String code, String message) {
        super(code, message);
    }
}
