package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;

@FunctionalInterface
public interface UpdateEmployeeUseCase {

    EmployeeResult update(UpdateEmployeeCommand command);
}
