package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Cargo que ocupa el empleado en el contrato (business-rules §3).
 * @param value cargo sin espacios en los extremos, de 3 a 100 caracteres
 */
public record JobTitle(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;

    /**
     * Quita los espacios de los extremos y valida la longitud.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta o no tiene entre 3 y
     *         100 caracteres ({@code VAL-001})
     */
    public JobTitle {
        value = value == null ? "" : value.strip();
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Job title must have between 3 and 100 characters.");
        }
    }
}
