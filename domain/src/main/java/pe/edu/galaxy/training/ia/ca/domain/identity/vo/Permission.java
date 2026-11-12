package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Acción autorizada con formato {@code <recurso>:<acción>[:<alcance>]}, p. ej. {@code contract:read:own}. */
public record Permission(String value) {

    // Cuantificadores posesivos (*+, ?+): sin retroceso, el motor no apila una llamada por repetición y una
    // entrada muy larga no puede desbordar la pila (java:S5998). Aceptan exactamente los mismos valores.
    private static final Pattern FORMAT = Pattern.compile("[a-z]+(?:-[a-z]+)*+:[a-z]+(?:-[a-z]+)*+(?::[a-z]+)?+");

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
