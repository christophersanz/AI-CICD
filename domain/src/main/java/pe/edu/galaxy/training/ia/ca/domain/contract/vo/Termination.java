package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.time.LocalDate;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException;

/** Término anticipado de un contrato (BR-CTR-009). */
public record Termination(LocalDate date, String reason) {

    // TODO business-rules.md §9: "¿Qué motivos de término se admiten?". Mientras tanto, texto libre obligatorio.
    public Termination {
        if (date == null) {
            throw new InvalidTerminationException("Termination date is required.");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidTerminationException("Termination reason is required.");
        }
        reason = reason.strip();
    }
}
