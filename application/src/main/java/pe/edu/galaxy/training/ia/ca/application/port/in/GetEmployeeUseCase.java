package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;

/** Consulta un empleado por su identificador (actor: quien tenga {@code employee:read}). */
@FunctionalInterface
public interface GetEmployeeUseCase {

    /**
     * Busca el empleado por su identificador.
     *
     * @param query identificador del empleado
     * @return los datos del empleado
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si el identificador no es
     *         un UUID válido ({@code VAL-001})
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException si no existe ({@code
     *         EMP-404})
     */
    EmployeeResult get(EmployeeQuery query);
}
