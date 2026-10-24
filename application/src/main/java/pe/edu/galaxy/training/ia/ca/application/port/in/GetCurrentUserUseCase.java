package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;

@FunctionalInterface
public interface GetCurrentUserUseCase {

    CurrentUserResult get(CurrentUserQuery query);
}
