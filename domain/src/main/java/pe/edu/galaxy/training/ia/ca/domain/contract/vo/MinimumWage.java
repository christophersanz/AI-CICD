package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.shared.vo.Money;

/**
 * Remuneración Mínima Vital (RMV) vigente. Su valor es configurable, nunca fijo en código (ADR-0002).
 *
 * @param amount monto en soles, mayor que cero
 */
public record MinimumWage(Money amount) {

    // TODO business-rules.md §9: "¿Solo moneda PEN?". Mientras tanto, la RMV se expresa en PEN.
    /**
     * Valida la moneda y el monto.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta, no está en
     *         {@code PEN} o es cero ({@code VAL-001})
     */
    public MinimumWage {
        if (amount == null || !amount.isIn(Money.PEN)) {
            throw new InvalidValueException("Minimum wage must be expressed in PEN.");
        }
        if (amount.isZero()) {
            throw new InvalidValueException("Minimum wage must be greater than zero.");
        }
    }

    /**
     * Crea la RMV en soles.
     * @param amount monto en soles, con 2 decimales como máximo (nunca se redondea)
     * @return la RMV
     */
    public static MinimumWage ofPen(BigDecimal amount) {
        return new MinimumWage(Money.pen(amount));
    }
}
