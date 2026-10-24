package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto.RefreshTokenHasher;

/**
 * Emite access tokens JWT (RS256, cabecera {@code kid}) con {@code sub}, {@code iss}, {@code aud},
 * {@code iat}, {@code exp}, {@code jti}, {@code username}, {@code roles} y {@code perms}, y genera
 * refresh tokens opacos con {@link RefreshTokenHasher}. Vidas útiles desde {@code security.jwt.*}.
 */
@Component
public class TokenIssuerAdapter implements TokenIssuerPort {

    private final JwtEncoder encoder;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SecurityProperties.Jwt properties;
    private final Clock clock;

    public TokenIssuerAdapter(JwtEncoder encoder, RefreshTokenHasher refreshTokenHasher,
                              SecurityProperties properties, Clock clock) {
        this.encoder = encoder;
        this.refreshTokenHasher = refreshTokenHasher;
        this.properties = properties.jwt();
        this.clock = clock;
    }

    @Override
    public IssuedAccessToken issueAccessToken(User user, Set<Permission> permissions) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.id().value().toString())
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaims.USERNAME, user.username().value())
                .claim(JwtClaims.ROLES, sorted(user.roles().stream().map(RoleName::value).toList()))
                .claim(JwtClaims.PERMISSIONS, sorted(permissions.stream().map(Permission::value).toList()))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(properties.keyId()).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, expiresAt);
    }

    @Override
    public IssuedRefreshToken generateRefreshToken() {
        String value = refreshTokenHasher.generate();
        return new IssuedRefreshToken(value, refreshTokenHasher.hash(value),
                clock.instant().plus(properties.refreshTokenTtl()));
    }

    @Override
    public String hashRefreshToken(String rawRefreshToken) {
        return refreshTokenHasher.hash(rawRefreshToken);
    }

    /** Orden estable para que el token sea legible y reproducible en pruebas. */
    private static List<String> sorted(Collection<String> values) {
        return values.stream().sorted().toList();
    }
}
