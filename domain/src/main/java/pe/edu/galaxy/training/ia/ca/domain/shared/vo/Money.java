package pe.edu.galaxy.training.ia.ca.domain.shared.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Monto no negativo con moneda, normalizado a 2 decimales. Nunca redondea: un monto con más decimales se rechaza.
 * Concepto del shared kernel (business-rules §2).
 *
 * @param amount monto con escala 2
 * @param currency moneda ISO 4217
 */
public record Money(BigDecimal amount, Currency currency) {

    /** Sol peruano, la única moneda que hoy aceptan {@code Salary} y {@code MinimumWage}. */
    public static final Currency PEN = Currency.getInstance("PEN");
    private static final int SCALE = 2;

    /**
     * Valida el monto y lo lleva a escala 2 sin redondear.
     * @throws InvalidValueException si falta el monto o la moneda, el monto es negativo o tiene más de 2 decimales
     *         ({@code VAL-001})
     */
    public Money {
        if (amount == null || currency == null) {
            throw new InvalidValueException("Money requires an amount and a currency.");
        }
        if (amount.signum() < 0) {
            throw new InvalidValueException("Money amount cannot be negative: " + amount);
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new InvalidValueException("Money amount cannot have more than 2 decimals: " + amount);
        }
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    /**
     * Crea un monto en la moneda indicada.
     * @param amount monto no negativo, con 2 decimales como máximo
     * @param currency moneda
     * @return el monto con escala 2
     * @throws InvalidValueException si el monto o la moneda no son válidos ({@code VAL-001})
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Crea un monto en soles.
     * @param amount monto no negativo, con 2 decimales como máximo
     * @return el monto en {@code PEN}
     * @throws InvalidValueException si el monto no es válido ({@code VAL-001})
     */
    public static Money pen(BigDecimal amount) {
        return new Money(amount, PEN);
    }

    /** {@return {@code true} si el monto es cero} */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Indica si el monto está en la moneda esperada.
     * @param expected moneda esperada
     * @return {@code true} si coincide
     */
    public boolean isIn(Currency expected) {
        return currency.equals(expected);
    }

    /**
     * Suma otro monto de la misma moneda.
     * @param other monto a sumar
     * @return un monto nuevo con la suma
     * @throws InvalidValueException si falta el otro monto o su moneda es distinta ({@code VAL-001})
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Resta otro monto de la misma moneda; el resultado no puede ser negativo.
     *
     * @param other monto a restar
     * @return un monto nuevo con la diferencia
     * @throws InvalidValueException si falta el otro monto, su moneda es distinta o el resultado sería negativo
     *         ({@code VAL-001})
     */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /**
     * Compara con otro monto de la misma moneda.
     * @param other monto con el que se compara
     * @return {@code true} si este monto es estrictamente menor
     * @throws InvalidValueException si falta el otro monto o su moneda es distinta ({@code VAL-001})
     */
    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Compara con otro monto de la misma moneda.
     * @param other monto con el que se compara
     * @return {@code true} si este monto es estrictamente mayor
     * @throws InvalidValueException si falta el otro monto o su moneda es distinta ({@code VAL-001})
     */
    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    private void ensureSameCurrency(Money other) {
        if (other == null) {
            throw new InvalidValueException("Cannot operate with a missing amount.");
        }
        if (!currency.equals(other.currency)) {
            throw new InvalidValueException("Cannot operate %s with %s.".formatted(currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
