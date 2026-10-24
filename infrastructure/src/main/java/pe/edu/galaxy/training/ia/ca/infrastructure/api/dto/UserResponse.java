package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Usuario creado")
public record UserResponse(
        UUID id,
        String username,
        Set<String> roles,
        boolean enabled) {
}
