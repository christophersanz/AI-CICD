package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;

public record CurrentUserResult(UUID id, String username, Set<String> roles, Set<String> permissions) {

    public static CurrentUserResult from(User user, Set<Permission> permissions) {
        return new CurrentUserResult(user.id().value(), user.username().value(), UserResult.roleNames(user),
                permissions.stream().map(Permission::value).collect(Collectors.toUnmodifiableSet()));
    }
}
