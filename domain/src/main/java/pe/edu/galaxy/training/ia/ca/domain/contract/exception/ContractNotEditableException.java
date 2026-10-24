package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class ContractNotEditableException extends DomainException {

    public ContractNotEditableException(String contractNumber, String status) {
        super("CTR-004", "Contract %s cannot be modified because it is %s.".formatted(contractNumber, status));
    }
}
