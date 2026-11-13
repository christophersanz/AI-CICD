package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;

/**
 * Reemplaza los nombres y el correo de un empleado (actor: quien tenga {@code employee:update}). El documento de
 * identidad no se modifica (BR-EMP-003).
 */
@FunctionalInterface
public interface UpdateEmployeeUseCase {

    /**
     * Reemplaza los nombres y el correo del empleado.
     *
     * @param command identificador del empleado y sus nombres, apellidos y correo nuevos
     * @return el empleado actualizado
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si el identificador, los
     *         nombres o el correo no cumplen su formato ({@code VAL-001})
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException si no existe ({@code
     *         EMP-404})
     */
    EmployeeResult update(UpdateEmployeeCommand command);
}
