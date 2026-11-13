package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Acción autorizada con formato {@code <recurso>:<acción>[:<alcance>]}, p. ej. {@code contract:read:own}.
 *
 * @param value permiso en minúsculas, sin espacios en los extremos
 */
public record Permission(String value) {

    // Cuantificadores posesivos (*+, ?+): sin retroceso, el motor no apila una llamada por repetición y una
    // entrada muy larga no puede desbordar la pila (java:S5998). Aceptan exactamente los mismos valores.
    private static final Pattern FORMAT = Pattern.compile("[a-z]+(?:-[a-z]+)*+:[a-z]+(?:-[a-z]+)*+(?::[a-z]+)?+");

    /**
     * Quita los espacios de los extremos y valida el formato.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no tiene el formato
     *         {@code <recurso>:<acción>[:<alcance>]} ({@code VAL-001})
     */
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
