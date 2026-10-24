package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

public record EmployeeId(UUID value) implements Identifier {

    public EmployeeId {
        if (value == null) {
            throw new InvalidValueException("EmployeeId value is required.");
        }
    }

    public static EmployeeId generate() {
        return new EmployeeId(UUID.randomUUID());
    }

    public static EmployeeId of(UUID value) {
        return new EmployeeId(value);
    }

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
