package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(RoleName roleName) {
        super("AUTH-009", "Role %s does not exist.".formatted(roleName.value()));
    }
}
