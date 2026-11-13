package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * La vigencia no es válida para el tipo de contrato: falta la fecha de inicio, un {@code FIXED_TERM} no tiene fin
 * posterior al inicio o un {@code INDEFINITE} tiene fecha de fin (BR-CTR-002, BR-CTR-003, {@code CTR-001}, HTTP 422).
 */
public class InvalidContractPeriodException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-001}.
     *
     * @param message qué parte de la vigencia no es válida
     */
    public InvalidContractPeriodException(String message) {
        super("CTR-001", message);
    }
}
