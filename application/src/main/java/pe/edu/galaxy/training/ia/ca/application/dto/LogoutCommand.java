package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

/** @param userId usuario autenticado que cierra la sesión */
public record LogoutCommand(UUID userId, String refreshToken) {

    @Override
    public String toString() {
        return "LogoutCommand[userId=%s, refreshToken=****]".formatted(userId);
    }
}
