package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;

/**
 * Crea el administrador inicial al arrancar la aplicación (actor: Sistema), solo si todavía no existe ningún
 * usuario con rol {@code ADMIN} (BR-USR-007).
 */
@FunctionalInterface
public interface BootstrapAdministratorUseCase {

    /**
     * Crea el administrador si todavía no hay ninguno; si ya hay un {@code ADMIN}, no hace nada.
     *
     * @param command usuario y contraseña tomados de las variables de entorno de arranque
     * @return {@code true} si creó el administrador; {@code false} si ya existía uno (BR-USR-007)
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException si el nombre de usuario
     *         ya lo usa alguien que no es {@code ADMIN} ({@code AUTH-007}); la aplicación no arranca
     * @throws pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException si la contraseña incumple
     *         la política (BR-USR-003, {@code AUTH-008}); la aplicación no arranca
     */
    boolean bootstrap(BootstrapAdministratorCommand command);
}
