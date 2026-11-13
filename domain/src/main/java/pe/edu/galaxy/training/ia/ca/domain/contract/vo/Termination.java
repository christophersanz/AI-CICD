package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.time.LocalDate;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException;

/**
 * Término anticipado de un contrato (BR-CTR-009).
 *
 * @param date fecha del término
 * @param reason motivo, sin espacios en los extremos
 */
public record Termination(LocalDate date, String reason) {

    // TODO business-rules.md §9: "¿Qué motivos de término se admiten?". Mientras tanto, texto libre obligatorio.
    /**
     * Exige fecha y motivo; quita los espacios del motivo. Que la fecha esté dentro de la vigencia lo valida
     * {@code Contract.terminate}.
     * @throws pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException si falta la fecha o el
     *         motivo ({@code CTR-007})
     */
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
