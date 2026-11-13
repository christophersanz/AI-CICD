package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/**
 * Se terminó anticipadamente un contrato {@code ACTIVE} (BR-CTR-009). Lo emite {@code Contract.terminate}.
 *
 * @param eventId identificador único del evento
 * @param occurredOn instante en que ocurrió
 * @param contractId contrato que lo emitió
 * @param terminationDate fecha del término, dentro de la vigencia
 * @param reason motivo del término
 */
public record ContractTerminated(UUID eventId, Instant occurredOn, ContractId contractId, LocalDate terminationDate, String reason)
        implements ContractEvent {
}
