package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Hash de contraseña con prefijo de algoritmo (p. ej. {@code {bcrypt}...}). Nunca la contraseña en claro. */
public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Password hash is required.");
        }
    }

    @Override
    public String toString() {
        return "PasswordHash[****]";
    }
}
