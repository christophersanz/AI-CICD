package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

public record JobTitle(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;

    public JobTitle {
        value = value == null ? "" : value.strip();
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Job title must have between 3 and 100 characters.");
        }
    }
}
