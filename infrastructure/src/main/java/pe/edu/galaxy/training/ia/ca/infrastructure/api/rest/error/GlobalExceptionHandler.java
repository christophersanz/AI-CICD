package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest.error;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.edu.galaxy.training.ia.ca.application.exception.ConflictException;
import pe.edu.galaxy.training.ia.ca.application.exception.NotFoundException;
import pe.edu.galaxy.training.ia.ca.application.exception.UnauthenticatedException;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractStatusTransitionException;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;

/** Traduce las excepciones a RFC 9457 Problem Details con el código de negocio en {@code code}. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_PREFIX = "urn:contract-api:errors:";

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
        HttpStatus status = ex instanceof InvalidContractStatusTransitionException
                ? HttpStatus.CONFLICT
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return problem(status, ex, ex.code());
    }

    /** {@code AUTH-005}: aunque es de dominio, no es una regla violada (422) sino una autenticación fallida. */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ProblemDetail> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex, ex.code());
    }

    /** {@code AUTH-002} y demás fallos de identidad detectados por los casos de uso. */
    @ExceptionHandler(UnauthenticatedException.class)
    ResponseEntity<ProblemDetail> handleUnauthenticated(UnauthenticatedException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex, ex.code());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex, ex.code());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex, ex.code());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.");
        body.setType(URI.create(TYPE_PREFIX + "internal-error"));
        body.setTitle("Internal error");
        return ResponseEntity.internalServerError().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail body = ex.getBody();
        body.setType(URI.create(TYPE_PREFIX + "invalid-request"));
        body.setTitle("Invalid request");
        body.setDetail("The request has invalid fields.");
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage())))
                .toList();
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, RuntimeException ex, String code) {
        String slug = slugOf(ex);
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        body.setType(URI.create(TYPE_PREFIX + slug));
        body.setTitle(titleOf(slug));
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }

    /** {@code ContractNotEditableException} → {@code contract-not-editable}. */
    static String slugOf(RuntimeException ex) {
        String name = ex.getClass().getSimpleName().replaceFirst("Exception$", "");
        return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

    private static String titleOf(String slug) {
        String words = slug.replace('-', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
