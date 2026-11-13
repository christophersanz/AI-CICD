package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Identificador del usuario; viaja en el {@code sub} del access token.
 * @param value UUID del usuario
 */
public record UserId(UUID value) implements Identifier {

    /**
     * Valida que el UUID esté presente.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    public UserId {
        if (value == null) {
            throw new InvalidValueException("UserId value is required.");
        }
    }

    /** {@return un identificador nuevo y aleatorio} */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Envuelve un UUID existente, por ejemplo el {@code sub} de un token.
     * @param value UUID del usuario
     * @return el identificador
     */
    public static UserId of(UUID value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
