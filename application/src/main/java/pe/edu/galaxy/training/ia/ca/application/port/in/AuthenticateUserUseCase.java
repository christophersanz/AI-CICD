package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;

@FunctionalInterface
public interface AuthenticateUserUseCase {

    TokenPairResult authenticate(LoginCommand command);
}
