package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;

public record UserResult(UUID id, String username, Set<String> roles, boolean enabled) {

    public static UserResult from(User user) {
        return new UserResult(user.id().value(), user.username().value(), roleNames(user), user.isEnabled());
    }

    static Set<String> roleNames(User user) {
        return user.roles().stream().map(RoleName::value).collect(Collectors.toUnmodifiableSet());
    }
}
