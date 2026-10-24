package pe.edu.galaxy.training.ia.ca.application.exception;

public abstract class ConflictException extends ApplicationException {

    protected ConflictException(String code, String message) {
        super(code, message);
    }
}
