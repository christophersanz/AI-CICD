package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Identificador del empleado, generado al registrarlo. Es lo único de {@code employee} que usan otros agregados
 * (ADR-0004).
 * @param value UUID del empleado
 */
public record EmployeeId(UUID value) implements Identifier {

    /**
     * Valida que el UUID esté presente.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    public EmployeeId {
        if (value == null) {
            throw new InvalidValueException("EmployeeId value is required.");
        }
    }

    /** {@return un identificador nuevo y aleatorio} */
    public static EmployeeId generate() {
        return new EmployeeId(UUID.randomUUID());
    }

    /**
     * Envuelve un UUID existente, por ejemplo uno leído de la base.
     * @param value UUID del empleado
     * @return el identificador
     */
    public static EmployeeId of(UUID value) {
        return new EmployeeId(value);
    }

    /**
     * Interpreta el texto de un UUID, por ejemplo el de una ruta de la API.
     * @param value texto del UUID
     * @return el identificador
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no es un UUID
     *         válido ({@code VAL-001})
     */
    public static EmployeeId of(String value) {
        if (value == null) {
            throw new InvalidValueException("EmployeeId value is required.");
        }
        try {
            return new EmployeeId(UUID.fromString(value.strip()));
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("EmployeeId must be a valid UUID: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
