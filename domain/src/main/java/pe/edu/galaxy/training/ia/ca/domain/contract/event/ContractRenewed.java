package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/**
 * Se extendió la fecha de fin de un contrato {@code FIXED_TERM} vigente (BR-CTR-007). Lo emite {@code Contract.renew}.
 *
 * @param eventId identificador único del evento
 * @param occurredOn instante en que ocurrió
 * @param contractId contrato que lo emitió
 * @param previousEndDate fecha de fin antes de la renovación
 * @param newEndDate fecha de fin nueva, posterior a la anterior
 */
public record ContractRenewed(UUID eventId, Instant occurredOn, ContractId contractId, LocalDate previousEndDate, LocalDate newEndDate)
        implements ContractEvent {
}
