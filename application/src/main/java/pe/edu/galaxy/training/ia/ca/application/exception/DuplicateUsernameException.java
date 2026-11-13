package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/** Ya existe un usuario con ese nombre, sin distinguir mayúsculas (BR-USR-001, {@code AUTH-007}, HTTP 409). */
public class DuplicateUsernameException extends ConflictException {

    /**
     * Crea el error con el código {@code AUTH-007}.
     *
     * @param username nombre de usuario repetido, ya normalizado
     */
    public DuplicateUsernameException(Username username) {
        super("AUTH-007", "A user with username %s already exists.".formatted(username.value()));
    }
}
