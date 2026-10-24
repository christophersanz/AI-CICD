package pe.edu.galaxy.training.ia.ca.infrastructure.security.authn;

import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException;
import pe.edu.galaxy.training.ia.ca.application.port.out.CredentialsAuthenticatorPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordPolicy;

/**
 * Verifica usuario y contraseña con el {@link AuthenticationManager}. Contraseña incorrecta, usuario
 * inexistente, deshabilitado o bloqueado salen todos como {@link InvalidCredentialsException}
 * (BR-USR-004, {@code AUTH-002}). Los fallos técnicos ({@code InternalAuthenticationServiceException})
 * no se enmascaran como credenciales inválidas.
 */
@Component
public class CredentialsAuthenticatorAdapter implements CredentialsAuthenticatorPort {

    private final AuthenticationManager authenticationManager;

    public CredentialsAuthenticatorAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public User authenticate(String username, String password) {
        if (exceedsBcryptLimit(password)) {
            throw new InvalidCredentialsException();
        }
        try {
            Authentication result = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            return ((UserAccountDetails) result.getPrincipal()).user();
        } catch (BadCredentialsException | UsernameNotFoundException | AccountStatusException ex) {
            throw new InvalidCredentialsException();
        }
    }

    /**
     * BCrypt solo compara los primeros 72 bytes: sin este control, una contraseña guardada de 72 bytes
     * seguida de cualquier sufijo sería aceptada. Ninguna contraseña válida supera ese límite
     * (BR-USR-003), así que se rechaza sin consultar al usuario.
     */
    private static boolean exceedsBcryptLimit(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length > PasswordPolicy.MAX_BYTES;
    }
}
