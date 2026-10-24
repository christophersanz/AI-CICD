package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Empleado registrado")
public record EmployeeResponse(
        UUID id,
        String documentType,
        String documentNumber,
        String firstNames,
        String lastNames,
        String email) {
}
