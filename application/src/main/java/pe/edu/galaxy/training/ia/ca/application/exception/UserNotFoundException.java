package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UserId userId) {
        super("AUTH-404", "User %s does not exist.".formatted(userId.value()));
    }
}
