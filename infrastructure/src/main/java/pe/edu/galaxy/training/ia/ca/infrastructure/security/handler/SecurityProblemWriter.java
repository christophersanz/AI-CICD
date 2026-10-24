package pe.edu.galaxy.training.ia.ca.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

/**
 * Escribe Problem Details (RFC 9457) desde los filtros de seguridad, que no pasan por el
 * {@code @RestControllerAdvice}. Mismo formato que {@code GlobalExceptionHandler}.
 */
public class SecurityProblemWriter {

    private static final String TYPE_PREFIX = "urn:contract-api:errors:";

    private final ObjectMapper objectMapper;

    public SecurityProblemWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, SecurityError error) throws IOException {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, error.detail());
        body.setType(URI.create(TYPE_PREFIX + error.slug()));
        body.setTitle(error.title());
        body.setProperty("code", error.code());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
