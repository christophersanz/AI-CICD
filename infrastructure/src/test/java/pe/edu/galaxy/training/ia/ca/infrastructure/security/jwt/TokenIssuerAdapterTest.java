package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto.RefreshTokenHasher;

class TokenIssuerAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
    private static final Clock FIXED = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final Set<Permission> PERMISSIONS =
            Set.of(new Permission("employee:create"), new Permission("contract:read:all"));

    private final JwtConfig config = new JwtConfig();
    private final RsaKeys keys = RsaKeys.from(jwtProperties("contract-api", "contract-api"));
    private final RefreshTokenHasher hasher = new RefreshTokenHasher();
    private final User user = User.create(new Username("ana"), new PasswordHash("{bcrypt}$2a$12$hash"),
            Set.of(RoleName.HR, RoleName.EMPLOYEE));

    private static SecurityProperties.Jwt jwtProperties(String issuer, String audience) {
        return new SecurityProperties.Jwt(issuer, audience, ACCESS_TTL, REFRESH_TTL, "kid-1", null, null, true);
    }

    private static SecurityProperties properties(SecurityProperties.Jwt jwt) {
        return new SecurityProperties(jwt, new SecurityProperties.Password("bcrypt", 4), null);
    }

    private TokenIssuerAdapter issuer(Clock clock) {
        return issuer(jwtProperties("contract-api", "contract-api"), clock);
    }

    private TokenIssuerAdapter issuer(SecurityProperties.Jwt jwt, Clock clock) {
        return new TokenIssuerAdapter(config.jwtEncoder(keys), hasher, properties(jwt), clock);
    }

    private JwtDecoder applicationDecoder() {
        return config.jwtDecoder(keys, properties(jwtProperties("contract-api", "contract-api")));
    }

    @Test
    void shouldEmitExpectedClaimsWhenAccessTokenIsIssued() throws Exception {
        // When
        IssuedAccessToken token = issuer(FIXED).issueAccessToken(user, PERMISSIONS);

        // Then
        JWTClaimsSet claims = SignedJWT.parse(token.value()).getJWTClaimsSet();
        assertThat(claims.getSubject()).isEqualTo(user.id().value().toString());
        assertThat(claims.getIssuer()).isEqualTo("contract-api");
        assertThat(claims.getAudience()).containsExactly("contract-api");
        assertThat(claims.getIssueTime()).isEqualTo(Date.from(NOW));
        assertThat(claims.getJWTID()).isNotBlank();
        assertThat(claims.getStringClaim(JwtClaims.USERNAME)).isEqualTo("ana");
        assertThat(claims.getStringListClaim(JwtClaims.ROLES)).containsExactly("EMPLOYEE", "HR");
        assertThat(claims.getStringListClaim(JwtClaims.PERMISSIONS))
                .containsExactly("contract:read:all", "employee:create");
    }

    @Test
    void shouldExpireAfterConfiguredTtlWhenAccessTokenIsIssued() throws Exception {
        // When
        IssuedAccessToken token = issuer(FIXED).issueAccessToken(user, PERMISSIONS);

        // Then
        assertThat(token.expiresAt()).isEqualTo(NOW.plus(ACCESS_TTL));
        assertThat(SignedJWT.parse(token.value()).getJWTClaimsSet().getExpirationTime())
                .isEqualTo(Date.from(NOW.plus(ACCESS_TTL)));
    }

    @Test
    void shouldSignWithRs256AndConfiguredKeyIdWhenAccessTokenIsIssued() throws Exception {
        // When
        IssuedAccessToken token = issuer(FIXED).issueAccessToken(user, PERMISSIONS);

        // Then
        SignedJWT jwt = SignedJWT.parse(token.value());
        assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
        assertThat(jwt.getHeader().getKeyID()).isEqualTo("kid-1");
    }

    @Test
    void shouldUseUniqueJtiWhenTwoTokensAreIssuedAtTheSameInstant() throws Exception {
        // When
        TokenIssuerAdapter adapter = issuer(FIXED);
        String first = SignedJWT.parse(adapter.issueAccessToken(user, PERMISSIONS).value()).getJWTClaimsSet().getJWTID();
        String second = SignedJWT.parse(adapter.issueAccessToken(user, PERMISSIONS).value()).getJWTClaimsSet().getJWTID();

        // Then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldBeAcceptedByApplicationDecoderAndMapPermissionsWhenTokenIsIssued() {
        // Given: el decoder valida exp contra el reloj real, así que se emite con un reloj real
        IssuedAccessToken token = issuer(Clock.systemUTC()).issueAccessToken(user, PERMISSIONS);

        // When
        Jwt decoded = applicationDecoder().decode(token.value());
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) config.jwtAuthenticationConverter().convert(decoded);

        // Then
        assertThat(authentication.getName()).isEqualTo(user.id().value().toString());
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("employee:create", "contract:read:all");
    }

    @Test
    void shouldBeRejectedByApplicationDecoderWhenTokenHasExpired() {
        // Given: emitido hace un día, muy fuera de la tolerancia de reloj de 60 s
        Clock yesterday = Clock.offset(Clock.systemUTC(), Duration.ofDays(-1));
        IssuedAccessToken token = issuer(yesterday).issueAccessToken(user, PERMISSIONS);

        // When / Then
        assertThatThrownBy(() -> applicationDecoder().decode(token.value()))
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldBeRejectedByApplicationDecoderWhenAudienceDiffers() {
        // Given
        IssuedAccessToken token = issuer(jwtProperties("contract-api", "another-api"), Clock.systemUTC())
                .issueAccessToken(user, PERMISSIONS);

        // When / Then
        assertThatThrownBy(() -> applicationDecoder().decode(token.value()))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void shouldBeRejectedByApplicationDecoderWhenIssuerDiffers() {
        // Given
        IssuedAccessToken token = issuer(jwtProperties("someone-else", "contract-api"), Clock.systemUTC())
                .issueAccessToken(user, PERMISSIONS);

        // When / Then
        assertThatThrownBy(() -> applicationDecoder().decode(token.value()))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void shouldGenerateOpaqueRefreshTokenWithItsHashAndTtlWhenRefreshTokenIsGenerated() {
        // When
        IssuedRefreshToken token = issuer(FIXED).generateRefreshToken();

        // Then
        assertThat(token.value()).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(token.hash()).isEqualTo(hasher.hash(token.value()));
        assertThat(token.expiresAt()).isEqualTo(NOW.plus(REFRESH_TTL));
    }

    @Test
    void shouldHashPresentedTokenLikeGeneratedOneWhenRefreshTokenIsHashed() {
        // Given
        TokenIssuerAdapter adapter = issuer(FIXED);
        IssuedRefreshToken token = adapter.generateRefreshToken();

        // When / Then
        assertThat(adapter.hashRefreshToken(token.value())).isEqualTo(token.hash());
    }

    @Test
    void shouldNotExposeTokenValuesWhenPrinted() {
        // When
        TokenIssuerAdapter adapter = issuer(FIXED);
        IssuedAccessToken access = adapter.issueAccessToken(user, PERMISSIONS);
        IssuedRefreshToken refresh = adapter.generateRefreshToken();

        // Then
        assertThat(access.toString()).doesNotContain(access.value());
        assertThat(refresh.toString()).doesNotContain(refresh.value()).doesNotContain(refresh.hash());
    }
}
