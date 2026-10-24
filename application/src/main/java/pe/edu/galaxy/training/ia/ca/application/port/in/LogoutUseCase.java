package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;

@FunctionalInterface
public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
