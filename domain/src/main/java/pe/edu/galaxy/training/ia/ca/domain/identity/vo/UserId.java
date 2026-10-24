package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

public record UserId(UUID value) implements Identifier {

    public UserId {
        if (value == null) {
            throw new InvalidValueException("UserId value is required.");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
