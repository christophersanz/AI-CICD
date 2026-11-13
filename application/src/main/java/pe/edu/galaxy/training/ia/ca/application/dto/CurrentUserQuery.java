package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

/**
 * Consulta del usuario autenticado.
 * @param userId identificador tomado del {@code sub} del access token
 */
public record CurrentUserQuery(UUID userId) {
}
