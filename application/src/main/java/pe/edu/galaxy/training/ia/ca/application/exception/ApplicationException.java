package pe.edu.galaxy.training.ia.ca.application.exception;

/**
 * Base de las excepciones que lanza la capa de aplicación al orquestar reglas que
 * requieren consultar el exterior (existencia, unicidad, concurrencia).
 */
public abstract class ApplicationException extends RuntimeException {

    /** Código de error del catálogo ({@code docs/business-rules.md} §8). */
    private final String code;

    /**
     * Crea la excepción con su código y su detalle técnico.
     *
     * @param code código de error del catálogo, por ejemplo {@code CTR-001}
     * @param message detalle técnico en inglés; la API lo traduce a Problem Details
     */
    protected ApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Código de error que la API envía en el campo {@code code} de Problem Details.
     * @return el código del catálogo
     */
    public String code() {
        return code;
    }
}
