package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;

/**
 * Consulta de un empleado por su identificador.
 * @param employeeId identificador del empleado
 */
public record EmployeeQuery(UUID employeeId) {
}
