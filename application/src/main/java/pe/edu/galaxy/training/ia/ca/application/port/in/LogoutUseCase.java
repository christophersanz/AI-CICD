package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;

/**
 * Cierra la sesión (actor: cualquier usuario autenticado) revocando toda la familia del refresh token presentado
 * (BR-USR-006). Es idempotente: si el token no existe o es de otro usuario, no hace nada y no lo revela.
 */
@FunctionalInterface
public interface LogoutUseCase {

    /**
     * Revoca la familia del refresh token si pertenece al usuario autenticado.
     *
     * @param command el refresh token presentado y el usuario autenticado que cierra la sesión
     */
    void logout(LogoutCommand command);
}
