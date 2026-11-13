package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Identificador de un refresh token; con él, cada token apunta a su sucesor.
 * @param value UUID del token
 */
public record RefreshTokenId(UUID value) implements Identifier {

    /**
     * Valida que el UUID esté presente.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    public RefreshTokenId {
        if (value == null) {
            throw new InvalidValueException("RefreshTokenId value is required.");
        }
    }

    /** {@return un identificador nuevo y aleatorio} */
    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    /**
     * Envuelve un UUID existente, por ejemplo uno leído de la base.
     * @param value UUID del token
     * @return el identificador
     */
    public static RefreshTokenId of(UUID value) {
        return new RefreshTokenId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
