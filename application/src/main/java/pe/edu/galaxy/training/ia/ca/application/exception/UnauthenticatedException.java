package pe.edu.galaxy.training.ia.ca.application.exception;

/** La identidad del solicitante no pudo verificarse. */
public abstract class UnauthenticatedException extends ApplicationException {

    protected UnauthenticatedException(String code, String message) {
        super(code, message);
    }
}
