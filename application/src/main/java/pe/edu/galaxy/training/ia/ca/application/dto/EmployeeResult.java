package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

/**
 * Datos de un empleado que devuelven los casos de uso, sin exponer el agregado.
 * @param id identificador del empleado
 * @param documentType tipo de documento
 * @param documentNumber número de documento
 * @param firstNames nombres
 * @param lastNames apellidos
 * @param email correo
 */
public record EmployeeResult(
        UUID id,
        DocumentType documentType,
        String documentNumber,
        String firstNames,
        String lastNames,
        String email) {

    /**
     * Copia los datos del agregado.
     * @param employee el empleado
     * @return el resultado
     */
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
