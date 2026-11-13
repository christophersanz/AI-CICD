package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import java.util.Set;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

/**
 * Rol y los permisos que agrupa. De solo lectura en esta versión: se carga con Flyway (business-rules §4.1).
 *
 * @param name nombre del rol
 * @param permissions permisos del rol, en un conjunto inmutable
 */
public record Role(RoleName name, Set<Permission> permissions) {

    /**
     * Exige el nombre y copia los permisos para que no cambien.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta el nombre o los
     *         permisos ({@code VAL-001})
     */
    public Role {
        Guard.notNull(name, "name");
        permissions = Set.copyOf(Guard.notNull(permissions, "permissions"));
    }
}
