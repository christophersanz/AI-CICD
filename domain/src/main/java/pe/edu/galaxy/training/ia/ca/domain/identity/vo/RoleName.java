package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.Locale;
import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Nombre de un rol. Es un valor abierto porque los roles se cargan desde la base de datos.
 *
 * @param value nombre en mayúsculas, de 2 a 30 caracteres entre letras y {@code _}
 */
public record RoleName(String value) {

    // Debe inicializarse antes que las constantes, que pasan por el constructor.
    private static final Pattern FORMAT = Pattern.compile("[A-Z][A-Z_]{1,29}");

    /** Administrador: todos los permisos. */
    public static final RoleName ADMIN = new RoleName("ADMIN");
    /** Recursos Humanos: gestiona empleados y contratos. */
    public static final RoleName HR = new RoleName("HR");
    /** Empleado: consulta solo sus propios contratos. */
    public static final RoleName EMPLOYEE = new RoleName("EMPLOYEE");

    /**
     * Normaliza el nombre a mayúsculas y valida el formato.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no tiene el formato
     *         ({@code VAL-001})
     */
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
