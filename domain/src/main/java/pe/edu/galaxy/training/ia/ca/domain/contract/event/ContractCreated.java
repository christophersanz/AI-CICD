package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import java.time.Instant;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

public record ContractCreated(UUID eventId, Instant occurredOn, ContractId contractId, EmployeeId employeeId,
                              ContractType type) implements ContractEvent {
}
