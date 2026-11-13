package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Número legible del contrato con formato {@code CTR-AAAA-NNNNNN} (BR-CTR-010).
 *
 * @param value número normalizado: sin espacios en los extremos y en mayúsculas
 */
public record ContractNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("CTR-(\\d{4})-(\\d{6})");
    private static final int MIN_YEAR = 1000;
    private static final int MAX_YEAR = 9999;
    private static final long MAX_SEQUENCE = 999_999;

    /**
     * Normaliza el número y valida su formato.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si no tiene el formato
     *         {@code CTR-AAAA-NNNNNN} ({@code VAL-001})
     */
    public ContractNumber {
        value = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Contract number must match CTR-YYYY-NNNNNN: " + value);
        }
    }

    /**
     * Interpreta un número escrito por una persona o leído de la base, sin distinguir mayúsculas.
     * @param value número de contrato
     * @return el número normalizado
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si no tiene el formato ({@code
     *         VAL-001})
     */
    public static ContractNumber parse(String value) {
        return new ContractNumber(value);
    }

    /**
     * Arma el número a partir del año y del correlativo que asigna el sistema.
     * @param year año de cuatro dígitos
     * @param sequence correlativo del año, entre 1 y 999999
     * @return el número, por ejemplo {@code CTR-2026-000123}
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si el año no tiene cuatro
     *         dígitos o el correlativo está fuera de rango ({@code VAL-001})
     */
    public static ContractNumber of(int year, long sequence) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new InvalidValueException("Contract number year must have 4 digits: " + year);
        }
        if (sequence < 1 || sequence > MAX_SEQUENCE) {
            throw new InvalidValueException("Contract number sequence must be between 1 and 999999: " + sequence);
        }
        return new ContractNumber("CTR-%04d-%06d".formatted(year, sequence));
    }

    /** {@return el año del número} */
    public int year() {
        return Integer.parseInt(parts().group(1));
    }

    /** {@return el correlativo del número dentro de su año} */
    public long sequence() {
        return Long.parseLong(parts().group(2));
    }

    private Matcher parts() {
        Matcher matcher = FORMAT.matcher(value);
        matcher.matches();
        return matcher;
    }

    @Override
    public String toString() {
        return value;
    }
}
