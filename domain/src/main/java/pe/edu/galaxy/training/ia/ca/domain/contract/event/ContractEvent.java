package pe.edu.galaxy.training.ia.ca.domain.contract.event;

import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;

/** Eventos del agregado {@code Contract} (business-rules.md §7). */
public sealed interface ContractEvent extends DomainEvent
        permits ContractCreated, ContractSigned, ContractRenewed, ContractTerminated, ContractExpired {

    /**
     * Contrato al que se refiere el evento; también es su {@link #aggregateId()}.
     * @return el identificador del contrato
     */
    ContractId contractId();

    @Override
    default ContractId aggregateId() {
        return contractId();
    }
}
