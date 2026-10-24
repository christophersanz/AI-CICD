package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import java.util.Set;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

/** Rol y los permisos que agrupa. De solo lectura en esta versión: se carga con Flyway. */
public record Role(RoleName name, Set<Permission> permissions) {

    public Role {
        Guard.notNull(name, "name");
        permissions = Set.copyOf(Guard.notNull(permissions, "permissions"));
    }
}
