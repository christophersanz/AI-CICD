package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

/** No existe un empleado con ese identificador ({@code EMP-404}, HTTP 404). */
public class EmployeeNotFoundException extends NotFoundException {

    /**
     * Crea el error con el código {@code EMP-404}.
     *
     * @param employeeId identificador buscado
     */
    public EmployeeNotFoundException(EmployeeId employeeId) {
        super("EMP-404", "Employee %s does not exist.".formatted(employeeId.value()));
    }
}
