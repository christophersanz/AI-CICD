package pe.edu.galaxy.training.ia.ca.infrastructure.security.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades {@code security.*}. Las claves llegan por variables de entorno, nunca desde el repositorio. */
@ConfigurationProperties("security")
public record SecurityProperties(Jwt jwt, Password password, Bootstrap bootstrap) {

    public SecurityProperties {
        bootstrap = bootstrap == null ? new Bootstrap(null) : bootstrap;
    }

    /**
     * @param publicKey            PEM X.509 ({@code -----BEGIN PUBLIC KEY-----})
     * @param privateKey           PEM PKCS#8 ({@code -----BEGIN PRIVATE KEY-----})
     * @param generateEphemeralKey solo para el perfil {@code local}: genera un par RSA al arrancar si
     *                             no hay claves; los tokens dejan de valer al reiniciar
     */
    public record Jwt(
            String issuer,
            String audience,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            String keyId,
            String publicKey,
            String privateKey,
            boolean generateEphemeralKey) {
    }

    /**
     * @param defaultAlgorithm algoritmo con el que se hashean las contraseñas nuevas ({@code bcrypt} o
     *                         {@code pbkdf2}); los hashes con otro algoritmo se re-hashean en el login
     * @param bcryptStrength   factor de coste de BCrypt (4–31); subirlo también provoca re-hash en el login
     */
    public record Password(String defaultAlgorithm, int bcryptStrength) {
    }

    /**
     * Administrador inicial (BR-USR-007). Solo por variables de entorno
     * ({@code SECURITY_BOOTSTRAP_ADMIN_USERNAME}, {@code SECURITY_BOOTSTRAP_ADMIN_PASSWORD}); sin valores
     * por defecto en ningún perfil.
     */
    public record Bootstrap(Admin admin) {

        public Bootstrap {
            admin = admin == null ? new Admin(null, null) : admin;
        }
    }

    public record Admin(String username, String password) {

        public boolean isConfigured() {
            return username != null && !username.isBlank() && password != null && !password.isBlank();
        }

        @Override
        public String toString() {
            return "Admin[username=%s, password=****]".formatted(username);
        }
    }
}
