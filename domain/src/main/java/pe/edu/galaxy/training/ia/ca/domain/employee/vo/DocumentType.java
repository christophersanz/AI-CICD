package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import java.util.regex.Pattern;

/** Tipo de documento de identidad, con la regla de formato definida para cada uno. */
public enum DocumentType {

    /** BR-EMP-002: exactamente 8 dígitos. */
    DNI(Pattern.compile("\\d{8}"), "DNI must have exactly 8 digits."),

    // TODO business-rules.md §9: "¿Qué formato tiene el carné de extranjería?". Hoy solo se exige que no esté vacío.
    FOREIGNER_CARD(Pattern.compile(".+"), "Foreigner card number is required.");

    private final Pattern format;
    private final String formatDescription;

    DocumentType(Pattern format, String formatDescription) {
        this.format = format;
        this.formatDescription = formatDescription;
    }

    public boolean isValidFormat(String value) {
        return value != null && format.matcher(value).matches();
    }

    public String formatDescription() {
        return formatDescription;
    }
}
