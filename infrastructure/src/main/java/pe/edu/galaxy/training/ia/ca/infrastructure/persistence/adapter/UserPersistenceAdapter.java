package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter;

import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.UserPersistenceMapper;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository.UserJpaRepository;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    static final String UNIQUE_USERNAME_CONSTRAINT = "uk_users_username";

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository repository, UserPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByUsername(Username username) {
        return repository.existsByUsername(username.value());
    }

    @Override
    public boolean existsByRole(RoleName roleName) {
        return repository.existsByRole(roleName.value());
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return repository.findById(userId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return repository.findByUsername(username.value()).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(user)));
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateUsername(ex)) {
                throw new DuplicateUsernameException(user.username());
            }
            throw ex;
        }
    }

    private static boolean isDuplicateUsername(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException violation
                && UNIQUE_USERNAME_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
    }
}
