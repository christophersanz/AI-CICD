package pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

class PasswordEncoderConfigTest {

    private static final String RAW = "correct horse battery";

    // Coste bajo solo para que las pruebas sean rápidas
    private static PasswordEncoder encoder(String algorithm, int strength) {
        return PasswordEncoderConfig.create(new SecurityProperties.Password(algorithm, strength));
    }

    @Test
    void shouldEncodeWithBcryptPrefixAndMatchWhenBcryptIsDefault() {
        // Given
        PasswordEncoder encoder = encoder("bcrypt", 4);

        // When
        String hash = encoder.encode(RAW);

        // Then
        assertThat(hash).startsWith("{bcrypt}$2a$04$");
        assertThat(encoder.matches(RAW, hash)).isTrue();
        assertThat(encoder.matches("wrong password!!", hash)).isFalse();
        assertThat(encoder.upgradeEncoding(hash)).isFalse();
    }

    @Test
    void shouldMatchAndRequestUpgradeWhenHashUsesNonDefaultAlgorithm() {
        // Given: hash heredado con PBKDF2 mientras el algoritmo por defecto es BCrypt
        String legacy = "{pbkdf2}" + Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(RAW);
        PasswordEncoder encoder = encoder("bcrypt", 4);

        // When / Then
        assertThat(encoder.matches(RAW, legacy)).isTrue();
        assertThat(encoder.upgradeEncoding(legacy)).isTrue();
    }

    @Test
    void shouldRequestUpgradeWhenBcryptStrengthIsIncreased() {
        // Given
        String weaker = "{bcrypt}" + new BCryptPasswordEncoder(4).encode(RAW);
        PasswordEncoder encoder = encoder("bcrypt", 5);

        // When / Then
        assertThat(encoder.matches(RAW, weaker)).isTrue();
        assertThat(encoder.upgradeEncoding(weaker)).isTrue();
        assertThat(encoder.upgradeEncoding(encoder.encode(RAW))).isFalse();
    }

    @Test
    void shouldEncodeWithPbkdf2PrefixWhenPbkdf2IsDefault() {
        PasswordEncoder encoder = encoder("pbkdf2", 4);

        String hash = encoder.encode(RAW);

        assertThat(hash).startsWith("{pbkdf2}");
        assertThat(encoder.matches(RAW, hash)).isTrue();
    }

    @Test
    void shouldFailFastWhenDefaultAlgorithmIsNotSupported() {
        assertThatThrownBy(() -> encoder("md5", 4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("md5");
    }

    @Test
    void shouldReturnPrefixedHashDifferentFromRawPasswordWhenAdapterHashes() {
        // Given
        PasswordHasherAdapter adapter = new PasswordHasherAdapter(encoder("bcrypt", 4));

        // When
        PasswordHash first = adapter.hash(RAW);
        PasswordHash second = adapter.hash(RAW);

        // Then: sal aleatoria, nunca la contraseña en claro
        assertThat(first.value()).startsWith("{bcrypt}").doesNotContain(RAW);
        assertThat(first).isNotEqualTo(second);
    }
}
