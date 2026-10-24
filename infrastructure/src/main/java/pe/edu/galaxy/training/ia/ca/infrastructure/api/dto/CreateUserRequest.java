package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Solo valida presencia y tamaños máximos. El formato del username (BR-USR-002), la política de
 * contraseñas (BR-USR-003) y la existencia de los roles los valida el núcleo.
 */
@Schema(description = "Datos para crear un usuario")
public record CreateUserRequest(
        @Schema(example = "ana.perez")
        @NotBlank @Size(max = 100) String username,

        @Schema(example = "una-contraseña-larga", format = "password")
        @NotBlank @Size(max = 256) String password,

        @Schema(example = "[\"HR\"]")
        @NotNull Set<@NotBlank @Size(max = 30) String> roles) {

    @Override
    public String toString() {
        return "CreateUserRequest[username=%s, password=****, roles=%s]".formatted(username, roles);
    }
}
