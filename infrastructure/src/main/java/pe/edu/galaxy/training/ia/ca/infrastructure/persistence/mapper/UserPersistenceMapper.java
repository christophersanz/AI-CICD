package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.UserJpaEntity;

/** Mapeo manual: el agregado solo se reconstruye con {@code restore}. */
@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id().value(),
                user.username().value(),
                user.passwordHash().value(),
                user.isEnabled(),
                user.roles().stream().map(RoleName::value).collect(Collectors.toSet()));
    }

    public User toDomain(UserJpaEntity entity) {
        return User.restore(
                UserId.of(entity.getId()),
                new Username(entity.getUsername()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRoles().stream().map(RoleName::new).collect(Collectors.toSet()),
                entity.isEnabled());
    }
}
