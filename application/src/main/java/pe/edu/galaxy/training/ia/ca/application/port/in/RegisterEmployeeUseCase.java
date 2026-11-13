package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.RegisterEmployeeCommand;

/** Registra un empleado nuevo en estado activo (actor: quien tenga {@code employee:create}). */
@FunctionalInterface
public interface RegisterEmployeeUseCase {

    /**
     * Valida los datos del empleado y lo registra.
     *
     * @param command tipo y número de documento, nombres, apellidos y correo
     * @return el empleado registrado, con su identificador generado
     * @throws pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException si el documento
     *         no tiene un formato válido, por ejemplo un DNI sin 8 dígitos (BR-EMP-002, {@code EMP-002})
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si los nombres o el correo
     *         no cumplen su formato ({@code VAL-001})
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException si ya existe un
     *         empleado con ese documento (BR-EMP-001, {@code EMP-001})
     */
    EmployeeResult register(RegisterEmployeeCommand command);
}
