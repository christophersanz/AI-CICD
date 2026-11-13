package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/**
 * Venció un contrato {@code FIXED_TERM} porque la fecha de corte superó su fecha de fin. Lo emite
 * {@code Contract.expire}, que ejecuta el Sistema (business-rules §5).
 *
 * @param eventId identificador único del evento
 * @param occurredOn instante en que ocurrió
 * @param contractId contrato que lo emitió
 * @param endDate fecha de fin con la que venció
 */
public record ContractExpired(UUID eventId, Instant occurredOn, ContractId contractId, LocalDate endDate)
        implements ContractEvent {
}
