package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;

@FunctionalInterface
public interface ListEmployeesUseCase {

    PageResult<EmployeeResult> list(EmployeeSearchQuery query);
}
