package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Hash de contraseña con prefijo de algoritmo (p. ej. {@code {bcrypt}...}). Nunca la contraseña en claro. Su
 * {@code toString()} oculta el valor para que no llegue a los logs.
 *
 * @param value hash con su prefijo
 */
public record PasswordHash(String value) {

    /**
     * Exige un hash no vacío.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o está en blanco
     *         ({@code VAL-001})
     */
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
