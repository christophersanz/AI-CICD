package pe.edu.galaxy.training.ia.ca.application.usecase;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.exception.RoleNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.CreateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordPolicy;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/** Crea un usuario con sus roles (BR-USR-001, BR-USR-002, BR-USR-003). */
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordHasherPort passwordHasher;

    public CreateUserService(UserRepositoryPort userRepository, RoleRepositoryPort roleRepository,
                             PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserResult create(CreateUserCommand command) {
        Username username = new Username(command.username());
        PasswordPolicy.validate(command.password());
        Set<RoleName> roles = Guard.notNull(command.roles(), "roles").stream()
                .map(RoleName::new)
                .collect(Collectors.toUnmodifiableSet());
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
        ensureRolesExist(roles);
        User user = User.create(username, passwordHasher.hash(command.password()), roles);
        return UserResult.from(userRepository.save(user));
    }

    private void ensureRolesExist(Set<RoleName> roles) {
        Set<RoleName> existing = roleRepository.findAllByNames(roles).stream()
                .map(Role::name)
                .collect(Collectors.toSet());
        roles.stream()
                .filter(role -> !existing.contains(role))
                .findFirst()
                .ifPresent(missing -> {
                    throw new RoleNotFoundException(missing);
                });
    }
}
