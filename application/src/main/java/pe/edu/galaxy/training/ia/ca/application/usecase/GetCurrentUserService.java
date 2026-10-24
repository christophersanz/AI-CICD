package pe.edu.galaxy.training.ia.ca.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;
import pe.edu.galaxy.training.ia.ca.application.exception.UserNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetCurrentUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

/** Devuelve el usuario autenticado con sus roles y permisos efectivos actuales. */
@Transactional(readOnly = true)
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;

    public GetCurrentUserService(UserRepositoryPort userRepository, RoleRepositoryPort roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public CurrentUserResult get(CurrentUserQuery query) {
        UserId userId = UserId.of(query.userId());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return CurrentUserResult.from(user, roleRepository.findPermissionsByRoles(user.roles()));
    }
}
