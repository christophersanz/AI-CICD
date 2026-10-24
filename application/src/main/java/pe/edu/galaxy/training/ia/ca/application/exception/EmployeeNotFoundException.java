package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

public class EmployeeNotFoundException extends NotFoundException {

    public EmployeeNotFoundException(EmployeeId employeeId) {
        super("EMP-404", "Employee %s does not exist.".formatted(employeeId.value()));
    }
}
