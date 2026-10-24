package pe.edu.galaxy.training.ia.ca.infrastructure.security.authn;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;

/**
 * Vista de Spring Security sobre el agregado {@link User}. Conserva el agregado para que el login lo
 * devuelva sin volver a consultarlo. Sin authorities: los permisos viajan en el access token.
 */
public record UserAccountDetails(User user) implements UserDetails {

    @Override
    public String getUsername() {
        return user.username().value();
    }

    @Override
    public String getPassword() {
        return user.passwordHash().value();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public String toString() {
        return "UserAccountDetails[username=%s, enabled=%s]".formatted(getUsername(), isEnabled());
    }
}
