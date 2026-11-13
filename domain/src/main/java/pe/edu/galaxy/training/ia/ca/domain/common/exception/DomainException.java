package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/**
 * Base de las excepciones de negocio del dominio. Cada subclase expone el código
 * de error del catálogo de {@code docs/business-rules.md}.
 */
public abstract class DomainException extends RuntimeException {

    /** Código de error del catálogo ({@code docs/business-rules.md} §8). */
    private final String code;

    /**
     * Crea la excepción con su código y su detalle técnico.
     *
     * @param code código de error del catálogo, por ejemplo {@code CTR-001}
     * @param message detalle técnico en inglés; la API lo traduce a Problem Details
     */
    protected DomainException(String code, String message) {
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
