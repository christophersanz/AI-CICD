package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.RegisterEmployeeCommand;

@FunctionalInterface
public interface RegisterEmployeeUseCase {

    EmployeeResult register(RegisterEmployeeCommand command);
}
