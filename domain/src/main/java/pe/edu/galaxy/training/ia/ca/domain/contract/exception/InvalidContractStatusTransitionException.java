package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class InvalidContractStatusTransitionException extends DomainException {

    public InvalidContractStatusTransitionException(String contractNumber, String status, String operation) {
        super("CTR-008", "Operation '%s' is not allowed for contract %s in status %s."
                .formatted(operation, contractNumber, status));
    }
}
