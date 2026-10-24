package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

/**
 * Emisión y validación de access tokens. Ambos usan el mismo {@link RsaKeys}; si no, el token emitido
 * no validaría (sobre todo con la clave efímera de {@code local}). La validación usa el resource server
 * de Spring (ADR-0007): firma, exp, nbf, iss y aud.
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtConfig {

    @Bean
    RsaKeys rsaKeys(SecurityProperties properties) {
        return RsaKeys.from(properties.jwt());
    }

    @Bean
    JwtDecoder jwtDecoder(RsaKeys keys, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keys.publicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        String audience = properties.jwt().audience();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.jwt().issuer()),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, aud -> aud != null && aud.contains(audience))));
        return decoder;
    }

    /** RS256 con la clave privada; el {@code kid} de la cabecera selecciona la clave del {@link JWKSet}. */
    @Bean
    JwtEncoder jwtEncoder(RsaKeys keys) {
        RSAKey key = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .keyID(keys.keyId())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    /** El claim {@code perms} se convierte en authorities sin prefijo ({@code contract:create}). */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter permissions = new JwtGrantedAuthoritiesConverter();
        permissions.setAuthoritiesClaimName(JwtClaims.PERMISSIONS);
        permissions.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(permissions);
        converter.setPrincipalClaimName(JwtClaimNames.SUB);
        return converter;
    }
}
