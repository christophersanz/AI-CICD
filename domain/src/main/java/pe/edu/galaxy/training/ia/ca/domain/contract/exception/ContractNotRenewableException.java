package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class ContractNotRenewableException extends DomainException {

    public ContractNotRenewableException(String contractNumber, String reason) {
        super("CTR-005", "Contract %s cannot be renewed: %s".formatted(contractNumber, reason));
    }
}
