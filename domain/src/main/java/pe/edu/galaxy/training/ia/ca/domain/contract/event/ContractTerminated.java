package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

public record ContractTerminated(UUID eventId, Instant occurredOn, ContractId contractId, LocalDate terminationDate, String reason)
        implements ContractEvent {
}
