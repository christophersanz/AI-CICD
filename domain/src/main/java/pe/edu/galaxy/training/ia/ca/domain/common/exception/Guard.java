package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/** Validaciones de presencia que lanzan {@link InvalidValueException} en lugar de {@code NullPointerException}. */
public final class Guard {

    private Guard() {
    }

    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new InvalidValueException(name + " is required.");
        }
        return value;
    }
}
