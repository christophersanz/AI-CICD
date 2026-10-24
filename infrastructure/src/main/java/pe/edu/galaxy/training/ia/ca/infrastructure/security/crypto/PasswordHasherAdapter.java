package pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;

/** Expone el {@link PasswordEncoder} de Spring a {@code application} como {@link PasswordHasherPort}. */
@Component
public class PasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        return new PasswordHash(passwordEncoder.encode(rawPassword));
    }
}
