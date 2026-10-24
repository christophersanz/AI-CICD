package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

class JwtConfigTest {

    private static SecurityProperties.Jwt jwtProperties(String publicKey, String privateKey, boolean ephemeral) {
        return new SecurityProperties.Jwt("contract-api", "contract-api", Duration.ofMinutes(15), Duration.ofDays(7),
                "kid-1", publicKey, privateKey, ephemeral);
    }

    @Test
    void shouldMapPermissionsClaimToAuthoritiesWithoutPrefixWhenTokenIsConverted() {
        // Given
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("6f1c2d1e-0000-0000-0000-000000000001")
                .claim(JwtClaims.PERMISSIONS, List.of("contract:create", "contract:read:all"))
                .claim(JwtClaims.ROLES, List.of("HR"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        // When
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) new JwtConfig().jwtAuthenticationConverter().convert(jwt);

        // Then
        assertThat(authentication.getName()).isEqualTo("6f1c2d1e-0000-0000-0000-000000000001");
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("contract:create", "contract:read:all");
    }

    @Test
    void shouldFailFastWhenKeysAreMissingAndEphemeralKeyIsDisabled() {
        assertThatThrownBy(() -> RsaKeys.from(jwtProperties("", "", false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SECURITY_JWT_PUBLIC_KEY");
    }

    @Test
    void shouldGenerateKeyPairWhenKeysAreMissingAndEphemeralKeyIsEnabled() {
        RsaKeys keys = RsaKeys.from(jwtProperties(null, null, true));

        assertThat(keys.publicKey().getModulus().bitLength()).isEqualTo(2048);
        assertThat(keys.keyId()).isEqualTo("kid-1");
    }

    @Test
    void shouldLoadPemKeysWhenTheyComeWithEscapedNewlines() {
        // Given: un par generado y exportado a PEM con "\n" escapados, como en una variable de entorno
        RsaKeys generated = RsaKeys.from(jwtProperties(null, null, true));
        String publicPem = pem("PUBLIC KEY", generated.publicKey().getEncoded());
        String privatePem = pem("PRIVATE KEY", generated.privateKey().getEncoded());

        // When
        RsaKeys loaded = RsaKeys.from(jwtProperties(publicPem, privatePem, false));

        // Then
        assertThat(loaded.publicKey()).isEqualTo(generated.publicKey());
        assertThat(loaded.privateKey().getModulus()).isEqualTo(generated.privateKey().getModulus());
    }

    @Test
    void shouldNotExposeKeysWhenPrinted() {
        assertThat(RsaKeys.from(jwtProperties(null, null, true)).toString()).isEqualTo("RsaKeys[keyId=kid-1]");
    }

    private static String pem(String type, byte[] der) {
        String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        String pem = "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----";
        return pem.replace("\n", "\\n");
    }
}
