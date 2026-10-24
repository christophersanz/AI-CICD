package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/**
 * Base de las excepciones de negocio del dominio. Cada subclase expone el código
 * de error del catálogo de {@code docs/business-rules.md}.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
