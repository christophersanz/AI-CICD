package pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

/**
 * {@link DelegatingPasswordEncoder} propio: cada hash lleva el prefijo de su algoritmo ({@code {bcrypt}},
 * {@code {pbkdf2}}). Se codifica con el algoritmo por defecto y se verifica con cualquiera de los
 * soportados. {@code upgradeEncoding} es verdadero si el hash usa otro algoritmo o un coste de BCrypt
 * menor al configurado, lo que permite re-hashear en el login. Argon2id llega en la fase 4.
 */
@Configuration
public class PasswordEncoderConfig {

    static final String BCRYPT = "bcrypt";
    static final String PBKDF2 = "pbkdf2";

    @Bean
    PasswordEncoder passwordEncoder(SecurityProperties properties) {
        return create(properties.password());
    }

    public static PasswordEncoder create(SecurityProperties.Password password) {
        Map<String, PasswordEncoder> encoders = Map.of(
                BCRYPT, new BCryptPasswordEncoder(password.bcryptStrength()),
                PBKDF2, Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        if (!encoders.containsKey(password.defaultAlgorithm())) {
            throw new IllegalStateException("Unsupported security.password.default-algorithm '%s'; use one of %s"
                    .formatted(password.defaultAlgorithm(), encoders.keySet()));
        }
        return new DelegatingPasswordEncoder(password.defaultAlgorithm(), encoders);
    }
}
