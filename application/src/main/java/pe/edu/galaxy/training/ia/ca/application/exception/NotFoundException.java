package pe.edu.galaxy.training.ia.ca.application.exception;

public abstract class NotFoundException extends ApplicationException {

    protected NotFoundException(String code, String message) {
        super(code, message);
    }
}
