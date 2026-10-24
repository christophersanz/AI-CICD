package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

public record UpdateEmployeeCommand(
        UUID employeeId,
        String firstNames,
        String lastNames,
        String email) {
}
