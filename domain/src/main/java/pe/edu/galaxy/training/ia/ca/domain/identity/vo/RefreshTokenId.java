package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

public record RefreshTokenId(UUID value) implements Identifier {

    public RefreshTokenId {
        if (value == null) {
            throw new InvalidValueException("RefreshTokenId value is required.");
        }
    }

    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId of(UUID value) {
        return new RefreshTokenId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
