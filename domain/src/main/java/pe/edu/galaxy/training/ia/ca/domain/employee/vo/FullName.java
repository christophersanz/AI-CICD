package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

public record FullName(String firstNames, String lastNames) {

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
