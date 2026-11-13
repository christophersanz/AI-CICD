package pe.edu.galaxy.training.ia.ca.application.dto;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

/**
 * Datos para registrar un empleado, sin validar: los valida el dominio al crear los value objects.
 * @param documentType tipo de documento
 * @param documentNumber número de documento
 * @param firstNames nombres
 * @param lastNames apellidos
 * @param email correo
 */
public record RegisterEmployeeCommand(
        DocumentType documentType,
        String documentNumber,
        String firstNames,
        String lastNames,
        String email) {
}
