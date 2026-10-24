package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

/**
 * Claims propios del access token. Además se usan los estándar: {@code sub} (id del usuario),
 * {@code iss}, {@code aud}, {@code iat}, {@code exp} y {@code jti}.
 */
public final class JwtClaims {

    /** Permisos efectivos; es lo que usa la autorización. */
    public static final String PERMISSIONS = "perms";
    /** Roles del usuario; solo informativo. */
    public static final String ROLES = "roles";
    public static final String USERNAME = "username";

    private JwtClaims() {
    }
}
