package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;

@FunctionalInterface
public interface RefreshTokenUseCase {

    TokenPairResult refresh(RefreshTokenCommand command);
}
