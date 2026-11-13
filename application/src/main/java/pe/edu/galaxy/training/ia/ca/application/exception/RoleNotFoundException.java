package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

/** Se pidió asignar un rol que no existe ({@code AUTH-009}, HTTP 404). */
public class RoleNotFoundException extends NotFoundException {

    /**
     * Crea el error con el código {@code AUTH-009}.
     *
     * @param roleName primer rol inexistente de la petición
     */
    public RoleNotFoundException(RoleName roleName) {
        super("AUTH-009", "Role %s does not exist.".formatted(roleName.value()));
    }
}
