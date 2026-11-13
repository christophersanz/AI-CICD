package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

/**
 * Datos para reemplazar los nombres y el correo de un empleado (BR-EMP-003).
 * @param employeeId identificador del empleado
 * @param firstNames nombres nuevos
 * @param lastNames apellidos nuevos
 * @param email correo nuevo
 */
public record UpdateEmployeeCommand(
        UUID employeeId,
        String firstNames,
        String lastNames,
        String email) {
}
