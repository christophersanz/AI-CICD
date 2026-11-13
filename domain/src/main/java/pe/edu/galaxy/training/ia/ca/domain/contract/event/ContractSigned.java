package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/**
 * El contrato pasó de {@code DRAFT} a {@code ACTIVE}. Lo emite {@code Contract.sign}.
 *
 * @param eventId identificador único del evento
 * @param occurredOn instante en que ocurrió
 * @param contractId contrato que lo emitió
 * @param startDate fecha de inicio de la vigencia
 */
public record ContractSigned(UUID eventId, Instant occurredOn, ContractId contractId, LocalDate startDate)
        implements ContractEvent {
}
