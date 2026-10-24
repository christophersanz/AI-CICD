package pe.edu.galaxy.training.ia.ca.domain.shared.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Monto no negativo con moneda, normalizado a 2 decimales. */
public record Money(BigDecimal amount, Currency currency) {

    public static final Currency PEN = Currency.getInstance("PEN");
    private static final int SCALE = 2;

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

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money pen(BigDecimal amount) {
        return new Money(amount, PEN);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isIn(Currency expected) {
        return currency.equals(expected);
    }

    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /** Resta otro monto; el resultado no puede ser negativo. */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

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
