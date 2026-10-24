package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        value = value == null ? "" : value.strip();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Email format is not valid.");
        }
    }
}
