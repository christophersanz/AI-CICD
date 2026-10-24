package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solo valida formato; las invariantes de negocio (p. ej. DNI de 8 dígitos) las valida el dominio. */
@Schema(description = "Datos para registrar un empleado")
public record EmployeeRequest(
        @Schema(example = "DNI", allowableValues = {"DNI", "FOREIGNER_CARD"})
        @NotBlank @Pattern(regexp = "DNI|FOREIGNER_CARD") String documentType,

        @Schema(example = "12345678")
        @NotBlank @Size(max = 20) String documentNumber,

        @Schema(example = "Ana María")
        @NotBlank @Size(max = 150) String firstNames,

        @Schema(example = "Pérez Gómez")
        @NotBlank @Size(max = 150) String lastNames,

        @Schema(example = "ana.perez@example.com")
        @NotBlank @Email @Size(max = 254) String email) {
}
