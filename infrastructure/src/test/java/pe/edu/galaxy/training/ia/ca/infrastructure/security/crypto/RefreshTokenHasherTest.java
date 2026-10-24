package pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RefreshTokenHasherTest {

    private final RefreshTokenHasher hasher = new RefreshTokenHasher();

    @Test
    void shouldGenerateUrlSafeTokensOf256BitsWhenCalled() {
        String token = hasher.generate();

        assertThat(token).hasSize(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void shouldGenerateDifferentTokensWhenCalledRepeatedly() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(hasher.generate());
        }
        assertThat(tokens).hasSize(1000);
    }

    @Test
    void shouldReturnStandardSha256HexWhenHashing() {
        // Vector de prueba de FIPS 180-2
        assertThat(hasher.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void shouldProduceSameHashForSameTokenAndDifferentForDifferentTokens() {
        String token = hasher.generate();

        assertThat(hasher.hash(token)).isEqualTo(hasher.hash(token)).hasSize(64);
        assertThat(hasher.hash(token)).isNotEqualTo(hasher.hash(hasher.generate()));
    }
}
