package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Número legible del contrato con formato {@code CTR-AAAA-NNNNNN} (BR-CTR-010). */
public record ContractNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("CTR-(\\d{4})-(\\d{6})");
    private static final int MIN_YEAR = 1000;
    private static final int MAX_YEAR = 9999;
    private static final long MAX_SEQUENCE = 999_999;

    public ContractNumber {
        value = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("Contract number must match CTR-YYYY-NNNNNN: " + value);
        }
    }

    public static ContractNumber parse(String value) {
        return new ContractNumber(value);
    }

    public static ContractNumber of(int year, long sequence) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new InvalidValueException("Contract number year must have 4 digits: " + year);
        }
        if (sequence < 1 || sequence > MAX_SEQUENCE) {
            throw new InvalidValueException("Contract number sequence must be between 1 and 999999: " + sequence);
        }
        return new ContractNumber("CTR-%04d-%06d".formatted(year, sequence));
    }

    public int year() {
        return Integer.parseInt(parts().group(1));
    }

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
