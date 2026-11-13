package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

/**
 * Cierre de sesión. Su {@code toString()} oculta el token.
 *
 * @param userId usuario autenticado que cierra la sesión
 * @param refreshToken refresh token cuya familia se revoca
 */
public record LogoutCommand(UUID userId, String refreshToken) {

    @Override
    public String toString() {
        return "LogoutCommand[userId=%s, refreshToken=****]".formatted(userId);
    }
}
