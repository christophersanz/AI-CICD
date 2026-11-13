package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.shared.vo.Money;

/**
 * Remuneración mensual bruta en soles.
 *
 * @param amount monto en soles, mayor que cero
 */
public record Salary(Money amount) {

    // TODO business-rules.md §9: "¿Solo moneda PEN?". Mientras tanto, Salary exige PEN según el glosario.
    /**
     * Valida la moneda y el monto.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta, no está en
     *         {@code PEN} o es cero ({@code VAL-001})
     */
    public Salary {
        if (amount == null || !amount.isIn(Money.PEN)) {
            throw new InvalidValueException("Salary must be expressed in PEN.");
        }
        if (amount.isZero()) {
            throw new InvalidValueException("Salary must be greater than zero.");
        }
    }

    /**
     * Crea la remuneración en soles.
     * @param amount monto en soles, con 2 decimales como máximo (nunca se redondea)
     * @return la remuneración
     */
    public static Salary ofPen(BigDecimal amount) {
        return new Salary(Money.pen(amount));
    }

    /**
     * Compara la remuneración con la RMV para aplicar BR-CTR-004 (la remuneración no puede ser menor).
     * TODO business-rules.md §9: tratamiento de contratos a tiempo parcial pendiente de definir.
     *
     * @param minimumWage RMV vigente
     * @return {@code true} si la remuneración es estrictamente menor que la RMV
     */
    public boolean isBelow(MinimumWage minimumWage) {
        return amount.isLessThan(minimumWage.amount());
    }
}
