package pe.edu.galaxy.training.ia.ca.application.dto;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

public record RegisterEmployeeCommand(
        DocumentType documentType,
        String documentNumber,
        String firstNames,
        String lastNames,
        String email) {
}
