package pe.edu.galaxy.training.ia.ca.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Responde 401 en Problem Details: {@code AUTH-004} si el token expiró y {@code AUTH-003} en cualquier
 * otro caso (ausente, mal formado, firma, emisor o audiencia). Conserva la cabecera
 * {@code WWW-Authenticate} de RFC 6750.
 */
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final BearerTokenAuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final SecurityProblemWriter writer;

    public ProblemDetailsAuthenticationEntryPoint(SecurityProblemWriter writer) {
        this.writer = writer;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        bearerEntryPoint.commence(request, response, exception);
        SecurityError error = isExpired(exception) ? SecurityError.TOKEN_EXPIRED : SecurityError.TOKEN_INVALID;
        writer.write(response, HttpStatus.UNAUTHORIZED, error);
    }

    /** {@code JwtTimestampValidator} describe la expiración como "Jwt expired at ...". */
    private static boolean isExpired(AuthenticationException exception) {
        return exception.getCause() instanceof JwtValidationException validation
                && validation.getErrors().stream()
                .anyMatch(error -> error.getDescription() != null
                        && error.getDescription().startsWith("Jwt expired"));
    }
}
