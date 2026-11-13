package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;

/**
 * Crea un usuario con sus roles (actor: quien tenga {@code user:manage}). El nombre de usuario se normaliza a
 * minúsculas y la contraseña se guarda solo como hash.
 */
@FunctionalInterface
public interface CreateUserUseCase {

    /**
     * Crea el usuario y guarda el hash de su contraseña.
     *
     * @param command nombre de usuario, contraseña en claro y nombres de los roles
     * @return el usuario creado, sin datos sensibles
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si el nombre de usuario no
     *         cumple el formato (BR-USR-002, {@code VAL-001})
     * @throws pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException si la contraseña incumple
     *         la política (BR-USR-003, {@code AUTH-008})
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException si ya existe ese nombre
     *         de usuario, sin distinguir mayúsculas (BR-USR-001, {@code AUTH-007})
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.RoleNotFoundException si alguno de los roles no
     *         existe ({@code AUTH-009})
     */
    UserResult create(CreateUserCommand command);
}
