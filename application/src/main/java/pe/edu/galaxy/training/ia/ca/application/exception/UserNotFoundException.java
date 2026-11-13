package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

/** No existe un usuario con ese identificador ({@code AUTH-404}, HTTP 404). */
public class UserNotFoundException extends NotFoundException {

    /**
     * Crea el error con el código {@code AUTH-404}.
     *
     * @param userId identificador buscado
     */
    public UserNotFoundException(UserId userId) {
        super("AUTH-404", "User %s does not exist.".formatted(userId.value()));
    }
}
