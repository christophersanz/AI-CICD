package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;

/**
 * Devuelve el usuario autenticado (actor: cualquier usuario autenticado, sin permiso explícito) con sus roles y sus
 * permisos efectivos leídos de la base, no los del token.
 */
@FunctionalInterface
public interface GetCurrentUserUseCase {

    /**
     * Carga el usuario del token con sus permisos actuales.
     *
     * @param query identificador del usuario tomado del token ({@code sub})
     * @return el usuario con sus roles y permisos vigentes
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.UserNotFoundException si el usuario del token ya no
     *         existe ({@code AUTH-404})
     */
    CurrentUserResult get(CurrentUserQuery query);
}
