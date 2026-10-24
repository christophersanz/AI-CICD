package pe.edu.galaxy.training.ia.ca.infrastructure.security.authn;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/**
 * Carga usuarios por {@link UserRepositoryPort} (no por el repositorio JPA) y persiste el re-hash
 * transparente que pide {@code DaoAuthenticationProvider} cuando {@code upgradeEncoding()} es verdadero.
 */
@Component
public class DbUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepositoryPort userRepository;

    public DbUserDetailsService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    /** Un username con formato inválido no puede existir: se trata como inexistente (BR-USR-004, AUTH-002). */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return new UserAccountDetails(findUser(username));
    }

    @Override
    public UserDetails updatePassword(UserDetails details, String newEncodedPassword) {
        User user = details instanceof UserAccountDetails account ? account.user() : findUser(details.getUsername());
        user.upgradePasswordHash(new PasswordHash(newEncodedPassword));
        return new UserAccountDetails(userRepository.save(user));
    }

    private User findUser(String username) {
        Username parsed;
        try {
            parsed = new Username(username);
        } catch (InvalidValueException ex) {
            throw new UsernameNotFoundException("User not found");
        }
        return userRepository.findByUsername(parsed)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
