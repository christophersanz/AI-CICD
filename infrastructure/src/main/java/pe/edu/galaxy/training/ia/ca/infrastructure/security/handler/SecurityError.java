package pe.edu.galaxy.training.ia.ca.infrastructure.security.handler;

/** Errores que producen los filtros de seguridad (catálogo de {@code business-rules.md}, §8). */
public enum SecurityError {

    ACCESS_DENIED("AUTH-001", "access-denied", "Access denied",
            "You do not have permission to access this resource."),
    TOKEN_INVALID("AUTH-003", "invalid-access-token", "Invalid access token",
            "A valid access token is required."),
    TOKEN_EXPIRED("AUTH-004", "expired-access-token", "Expired access token",
            "The access token has expired.");

    private final String code;
    private final String slug;
    private final String title;
    private final String detail;

    SecurityError(String code, String slug, String title, String detail) {
        this.code = code;
        this.slug = slug;
        this.title = title;
        this.detail = detail;
    }

    public String code() {
        return code;
    }

    public String slug() {
        return slug;
    }

    public String title() {
        return title;
    }

    public String detail() {
        return detail;
    }
}
