package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solo valida presencia y un tamaño máximo. El formato del username no se valida aquí: uno inválido
 * responde igual que unas credenciales incorrectas ({@code AUTH-002}).
 */
@Schema(description = "Credenciales de inicio de sesión")
public record LoginRequest(
        @Schema(example = "admin")
        @NotBlank @Size(max = 100) String username,

        @Schema(example = "una-contraseña-larga", format = "password")
        @NotBlank @Size(max = 256) String password) {

    @Override
    public String toString() {
        return "LoginRequest[username=%s, password=****]".formatted(username);
    }
}
