package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.time.LocalDate;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

/** Registro histórico de una renovación (BR-CTR-007). */
public record ContractRenewal(LocalDate previousEndDate, LocalDate newEndDate) {

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
