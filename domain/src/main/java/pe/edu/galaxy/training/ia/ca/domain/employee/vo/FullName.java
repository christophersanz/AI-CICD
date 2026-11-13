package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Nombres y apellidos del empleado, por separado para ordenar y buscar por apellidos (BR-EMP-004).
 * @param firstNames nombres, sin espacios en los extremos
 * @param lastNames apellidos, sin espacios en los extremos
 */
public record FullName(String firstNames, String lastNames) {

    /**
     * Exige ambas partes y les quita los espacios de los extremos.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta alguna o está en
     *         blanco ({@code VAL-001})
     */
    public FullName {
        if (firstNames == null || firstNames.isBlank()) {
            throw new InvalidValueException("First names are required.");
        }
        if (lastNames == null || lastNames.isBlank()) {
            throw new InvalidValueException("Last names are required.");
        }
        firstNames = firstNames.strip();
        lastNames = lastNames.strip();
    }
}
