package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/** La remuneración es menor que la RMV vigente (BR-CTR-004, {@code CTR-002}, HTTP 422). */
public class SalaryBelowMinimumWageException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-002}.
     *
     * @param salary remuneración mensual propuesta
     * @param minimumWage RMV vigente configurada
     */
    public SalaryBelowMinimumWageException(BigDecimal salary, BigDecimal minimumWage) {
        super("CTR-002", "Salary %s is below the current minimum wage %s.".formatted(salary, minimumWage));
    }
}
