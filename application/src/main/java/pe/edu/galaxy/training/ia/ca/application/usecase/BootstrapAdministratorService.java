package pe.edu.galaxy.training.ia.ca.application.usecase;

import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.port.in.BootstrapAdministratorUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordPolicy;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/** Crea el administrador inicial si todavía no hay ningún usuario con rol {@code ADMIN} (BR-USR-007). */
@Transactional
public class BootstrapAdministratorService implements BootstrapAdministratorUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public BootstrapAdministratorService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public boolean bootstrap(BootstrapAdministratorCommand command) {
        if (userRepository.existsByRole(RoleName.ADMIN)) {
            return false;
        }
        Username username = new Username(command.username());
        PasswordPolicy.validate(command.password());
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
        userRepository.save(User.create(username, passwordHasher.hash(command.password()), Set.of(RoleName.ADMIN)));
        return true;
    }
}
