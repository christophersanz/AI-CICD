package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.time.LocalDate;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/**
 * Registro histórico de una renovación (BR-CTR-007).
 *
 * @param previousEndDate fecha de fin antes de renovar
 * @param newEndDate fecha de fin después de renovar, posterior a la anterior
 */
public record ContractRenewal(LocalDate previousEndDate, LocalDate newEndDate) {

    /**
     * Valida que ambas fechas existan y que la nueva sea posterior.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta una fecha o la nueva
     *         no es posterior ({@code VAL-001})
     */
    public ContractRenewal {
        if (previousEndDate == null || newEndDate == null) {
            throw new InvalidValueException("A renewal requires the previous and the new end date.");
        }
        if (!newEndDate.isAfter(previousEndDate)) {
            throw new InvalidValueException("The new end date %s must be after %s."
                    .formatted(newEndDate, previousEndDate));
        }
    }
}
