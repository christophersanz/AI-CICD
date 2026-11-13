package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;

/**
 * Usuario autenticado con sus permisos efectivos, leídos de la base y no del token.
 * @param id identificador del usuario
 * @param username nombre de usuario normalizado
 * @param roles nombres de sus roles
 * @param permissions permisos efectivos: la unión de los de sus roles
 */
public record CurrentUserResult(UUID id, String username, Set<String> roles, Set<String> permissions) {

    /**
     * Arma el resultado a partir del agregado y de sus permisos efectivos.
     * @param user usuario autenticado
     * @param permissions permisos efectivos del usuario
     * @return el resultado
     */
    public static CurrentUserResult from(User user, Set<Permission> permissions) {
        return new CurrentUserResult(user.id().value(), user.username().value(), UserResult.roleNames(user),
                permissions.stream().map(Permission::value).collect(Collectors.toUnmodifiableSet()));
    }
}
