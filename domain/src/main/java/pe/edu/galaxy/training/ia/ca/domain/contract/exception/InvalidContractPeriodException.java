package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

public class InvalidContractPeriodException extends DomainException {

    public InvalidContractPeriodException(String message) {
        super("CTR-001", message);
    }
}
