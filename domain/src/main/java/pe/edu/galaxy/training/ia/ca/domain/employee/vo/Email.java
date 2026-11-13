package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Correo del empleado. Solo se valida la forma {@code usuario@dominio.tld}, sin comprobar que exista.
 * @param value correo sin espacios en los extremos
 */
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Quita los espacios de los extremos y valida la forma.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no tiene la forma
     *         {@code usuario@dominio.tld} ({@code VAL-001})
     */
    public Email {
        value = value == null ? "" : value.strip();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Email format is not valid.");
        }
    }
}
