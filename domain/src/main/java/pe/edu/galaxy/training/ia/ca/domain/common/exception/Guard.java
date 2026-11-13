package pe.edu.galaxy.training.ia.ca.domain.common.exception;

/** Validaciones de presencia que lanzan {@link InvalidValueException} en lugar de {@code NullPointerException}. */
public final class Guard {

    private Guard() {
    }

    /**
     * Exige que un valor obligatorio esté presente.
     * @param <T> tipo del valor
     * @param value valor recibido
     * @param name nombre del dato, para el mensaje de error
     * @return el mismo valor, para encadenar la asignación
     * @throws InvalidValueException si el valor es {@code null} ({@code VAL-001})
     */
    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new InvalidValueException(name + " is required.");
        }
        return value;
    }
}
