package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;

@FunctionalInterface
public interface CreateUserUseCase {

    UserResult create(CreateUserCommand command);
}
