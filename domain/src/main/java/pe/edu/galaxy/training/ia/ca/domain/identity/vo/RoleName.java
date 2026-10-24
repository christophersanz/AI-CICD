package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.Locale;
import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Nombre de un rol. Es un valor abierto porque los roles se cargan desde la base de datos. */
public record RoleName(String value) {

    // Debe inicializarse antes que las constantes, que pasan por el constructor.
    private static final Pattern FORMAT = Pattern.compile("[A-Z][A-Z_]{1,29}");

    public static final RoleName ADMIN = new RoleName("ADMIN");
    public static final RoleName HR = new RoleName("HR");
    public static final RoleName EMPLOYEE = new RoleName("EMPLOYEE");

    public RoleName {
        value = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Role name is not valid: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
