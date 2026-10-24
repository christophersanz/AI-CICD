package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Solo valida formato; el documento de identidad no forma parte de la actualización (BR-EMP-003). */
@Schema(description = "Datos para actualizar un empleado")
public record UpdateEmployeeRequest(
        @Schema(example = "Ana María")
        @NotBlank @Size(max = 150) String firstNames,

        @Schema(example = "Pérez Gómez")
        @NotBlank @Size(max = 150) String lastNames,

        @Schema(example = "ana.perez@example.com")
        @NotBlank @Email @Size(max = 254) String email) {
}
