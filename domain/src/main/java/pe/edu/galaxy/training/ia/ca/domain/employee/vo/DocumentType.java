package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.regex.Pattern;

/** Tipo de documento de identidad, con la regla de formato definida para cada uno. */
public enum DocumentType {

    /** BR-EMP-002: exactamente 8 dígitos. */
    DNI(Pattern.compile("\\d{8}"), "DNI must have exactly 8 digits."),

    // TODO business-rules.md §9: "¿Qué formato tiene el carné de extranjería?". Hoy solo se exige que no esté vacío.
    /** Carné de extranjería: por ahora basta con que no esté vacío. */
    FOREIGNER_CARD(Pattern.compile(".+"), "Foreigner card number is required.");

    private final Pattern format;
    private final String formatDescription;

    DocumentType(Pattern format, String formatDescription) {
        this.format = format;
        this.formatDescription = formatDescription;
    }

    /**
     * Comprueba el número contra la regla de formato de este tipo.
     * @param value número sin espacios en los extremos
     * @return {@code true} si cumple el formato; {@code false} si falta o no lo cumple
     */
    public boolean isValidFormat(String value) {
        return value != null && format.matcher(value).matches();
    }

    /** {@return la regla de formato en inglés, para el detalle del error} */
    public String formatDescription() {
        return formatDescription;
    }
}
