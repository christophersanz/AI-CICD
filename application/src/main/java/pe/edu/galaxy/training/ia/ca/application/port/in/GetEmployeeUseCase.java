package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;

@FunctionalInterface
public interface GetEmployeeUseCase {

    EmployeeResult get(EmployeeQuery query);
}
