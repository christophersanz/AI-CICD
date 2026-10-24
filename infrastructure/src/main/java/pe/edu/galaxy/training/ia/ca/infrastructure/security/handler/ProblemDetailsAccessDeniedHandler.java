package pe.edu.galaxy.training.ia.ca.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Responde 403 {@code AUTH-001} en Problem Details. */
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityProblemWriter writer;

    public ProblemDetailsAccessDeniedHandler(SecurityProblemWriter writer) {
        this.writer = writer;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        writer.write(response, HttpStatus.FORBIDDEN, SecurityError.ACCESS_DENIED);
    }
}
