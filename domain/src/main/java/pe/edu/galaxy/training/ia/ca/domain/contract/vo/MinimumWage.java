package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.shared.vo.Money;

/** Remuneración Mínima Vital (RMV) vigente. Su valor es configurable, nunca fijo en código (ADR-0002). */
public record MinimumWage(Money amount) {

    // TODO business-rules.md §9: "¿Solo moneda PEN?". Mientras tanto, la RMV se expresa en PEN.
    public MinimumWage {
        if (amount == null || !amount.isIn(Money.PEN)) {
            throw new InvalidValueException("Minimum wage must be expressed in PEN.");
        }
        if (amount.isZero()) {
            throw new InvalidValueException("Minimum wage must be greater than zero.");
        }
    }

    public static MinimumWage ofPen(BigDecimal amount) {
        return new MinimumWage(Money.pen(amount));
    }
}
