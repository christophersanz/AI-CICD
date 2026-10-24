package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.shared.vo.Money;

/** Remuneración mensual bruta en soles. */
public record Salary(Money amount) {

    // TODO business-rules.md §9: "¿Solo moneda PEN?". Mientras tanto, Salary exige PEN según el glosario.
    public Salary {
        if (amount == null || !amount.isIn(Money.PEN)) {
            throw new InvalidValueException("Salary must be expressed in PEN.");
        }
        if (amount.isZero()) {
            throw new InvalidValueException("Salary must be greater than zero.");
        }
    }

    public static Salary ofPen(BigDecimal amount) {
        return new Salary(Money.pen(amount));
    }

    /**
     * BR-CTR-004: la remuneración no puede ser menor que la RMV.
     * TODO business-rules.md §9: tratamiento de contratos a tiempo parcial pendiente de definir.
     */
    public boolean isBelow(MinimumWage minimumWage) {
        return amount.isLessThan(minimumWage.amount());
    }
}
