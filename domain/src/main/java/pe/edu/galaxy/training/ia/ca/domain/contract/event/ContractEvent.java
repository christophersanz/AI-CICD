package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/** Eventos del agregado {@code Contract} (business-rules.md §7). */
public sealed interface ContractEvent extends DomainEvent
        permits ContractCreated, ContractSigned, ContractRenewed, ContractTerminated, ContractExpired {

    ContractId contractId();

    @Override
    default ContractId aggregateId() {
        return contractId();
    }
}
