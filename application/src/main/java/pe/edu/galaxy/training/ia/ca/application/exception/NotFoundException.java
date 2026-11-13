package pe.edu.galaxy.training.ia.ca.application.exception;

/** El recurso pedido no existe. La API responde 404. */
public abstract class NotFoundException extends ApplicationException {

    /**
     * Crea la excepción con su código y su detalle técnico.
     *
     * @param code código de error del catálogo
     * @param message detalle técnico en inglés
     */
    protected NotFoundException(String code, String message) {
        super(code, message);
    }
}
