package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Optional;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

public interface UserRepositoryPort {

    boolean existsByUsername(Username username);

    boolean existsByRole(RoleName roleName);

    Optional<User> findById(UserId userId);

    Optional<User> findByUsername(Username username);

    /**
     * Persiste el usuario.
     *
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException
     *         si otro usuario con el mismo nombre se guardó concurrentemente
     */
    User save(User user);
}
