package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.List;
import java.util.Set;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

public interface RoleRepositoryPort {

    /** Devuelve los roles existentes entre los solicitados; los inexistentes se omiten. */
    List<Role> findAllByNames(Set<RoleName> names);

    /** Unión de los permisos de los roles indicados. */
    Set<Permission> findPermissionsByRoles(Set<RoleName> names);
}
