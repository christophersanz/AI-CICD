package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Usuario autenticado con sus roles y permisos efectivos")
public record CurrentUserResponse(
        UUID id,
        String username,
        List<String> roles,
        List<String> permissions) {
}
