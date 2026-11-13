package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

/**
 * Se creó un contrato en {@code DRAFT}. Lo emite {@code Contract.create}.
 *
 * @param eventId identificador único del evento
 * @param occurredOn instante en que ocurrió
 * @param contractId contrato que lo emitió
 * @param employeeId empleado del contrato
 * @param type tipo de contrato
 */
public record ContractCreated(UUID eventId, Instant occurredOn, ContractId contractId, EmployeeId employeeId,
                              ContractType type) implements ContractEvent {
}
