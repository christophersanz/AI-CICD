package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

public record EmployeeResult(
        UUID id,
        DocumentType documentType,
        String documentNumber,
        String firstNames,
        String lastNames,
        String email) {

    public static EmployeeResult from(Employee employee) {
        return new EmployeeResult(
                employee.id().value(),
                employee.documentNumber().type(),
                employee.documentNumber().value(),
                employee.fullName().firstNames(),
                employee.fullName().lastNames(),
                employee.email().value());
    }
}
