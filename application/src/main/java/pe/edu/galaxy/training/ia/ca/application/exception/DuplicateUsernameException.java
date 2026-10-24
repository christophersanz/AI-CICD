package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

public class DuplicateUsernameException extends ConflictException {

    public DuplicateUsernameException(Username username) {
        super("AUTH-007", "A user with username %s already exists.".formatted(username.value()));
    }
}
