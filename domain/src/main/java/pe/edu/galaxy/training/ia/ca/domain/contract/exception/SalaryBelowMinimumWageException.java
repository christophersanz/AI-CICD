package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import java.math.BigDecimal;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class SalaryBelowMinimumWageException extends DomainException {

    public SalaryBelowMinimumWageException(BigDecimal salary, BigDecimal minimumWage) {
        super("CTR-002", "Salary %s is below the current minimum wage %s.".formatted(salary, minimumWage));
    }
}
