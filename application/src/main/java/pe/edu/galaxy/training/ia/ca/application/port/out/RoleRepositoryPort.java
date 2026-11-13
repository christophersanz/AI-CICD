package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.List;
import java.util.Set;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

/** Lectura de roles y permisos, que se guardan en la base de datos (business-rules §4.1). */
public interface RoleRepositoryPort {

    /**
     * Devuelve los roles existentes entre los solicitados; los inexistentes se omiten, para que quien llama detecte
     * cuáles faltan.
     *
     * @param names nombres de los roles buscados
     * @return los roles encontrados; vacío si no existe ninguno
     */
    List<Role> findAllByNames(Set<RoleName> names);

    /**
     * Calcula los permisos efectivos de un usuario.
     *
     * @param names roles del usuario
     * @return la unión de los permisos de esos roles; vacío si no tienen ninguno
     */
    Set<Permission> findPermissionsByRoles(Set<RoleName> names);
}
