package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.Locale;
import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Nombre de inicio de sesión, normalizado a minúsculas (BR-USR-002). Es único sin distinguir mayúsculas (BR-USR-001).
 *
 * @param value nombre en minúsculas, de 3 a 50 caracteres: letras, dígitos, {@code .}, {@code _} o {@code -}
 */
public record Username(String value) {

    private static final Pattern FORMAT = Pattern.compile("[a-z0-9._-]{3,50}");

    /**
     * Quita los espacios, pasa a minúsculas y valida el formato.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no cumple el
     *         formato ({@code VAL-001})
     */
    public Username {
        value = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException(
                    "Username must have 3-50 characters: lowercase letters, digits, '.', '_' or '-'.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
