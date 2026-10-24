package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Acción autorizada con formato {@code <recurso>:<acción>[:<alcance>]}, p. ej. {@code contract:read:own}. */
public record Permission(String value) {

    private static final Pattern FORMAT = Pattern.compile("[a-z]+(-[a-z]+)*:[a-z]+(-[a-z]+)*(:[a-z]+)?");

    public Permission {
        value = value == null ? "" : value.strip();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Permission must follow <resource>:<action>[:<scope>]: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
