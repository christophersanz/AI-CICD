package pe.edu.galaxy.training.ia.ca.application.exception;

/**
 * Base de las excepciones que lanza la capa de aplicación al orquestar reglas que
 * requieren consultar el exterior (existencia, unicidad, concurrencia).
 */
public abstract class ApplicationException extends RuntimeException {

    private final String code;

    protected ApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
