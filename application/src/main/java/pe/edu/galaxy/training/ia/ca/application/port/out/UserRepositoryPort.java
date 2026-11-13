package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Optional;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/** Persistencia del agregado {@code User}. Los nombres de usuario llegan ya normalizados a minúsculas. */
public interface UserRepositoryPort {

    /**
     * Indica si el nombre de usuario ya está ocupado (BR-USR-001).
     * @param username nombre de usuario normalizado
     * @return {@code true} si existe un usuario con ese nombre
     */
    boolean existsByUsername(Username username);

    /**
     * Indica si algún usuario tiene el rol, para decidir si hace falta el administrador inicial (BR-USR-007).
     * @param roleName rol buscado
     * @return {@code true} si al menos un usuario lo tiene
     */
    boolean existsByRole(RoleName roleName);

    /**
     * Carga el usuario con sus roles.
     * @param userId identificador del usuario
     * @return el usuario, o vacío si no existe
     */
    Optional<User> findById(UserId userId);

    /**
     * Carga el usuario con sus roles.
     * @param username nombre de usuario normalizado
     * @return el usuario, o vacío si no existe
     */
    Optional<User> findByUsername(Username username);

    /**
     * Inserta el usuario o actualiza el existente con el mismo identificador.
     *
     * @param user el agregado a guardar
     * @return el usuario tal como quedó guardado
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException
     *         si otro usuario con el mismo nombre se guardó concurrentemente
     */
    User save(User user);
}
