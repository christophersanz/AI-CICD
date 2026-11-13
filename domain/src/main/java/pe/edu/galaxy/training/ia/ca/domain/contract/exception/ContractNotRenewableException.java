package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * El contrato no puede renovarse: no es {@code FIXED_TERM} en {@code ACTIVE} o la nueva fecha de fin no es posterior a
 * la actual (BR-CTR-007, {@code CTR-005}, HTTP 422).
 */
public class ContractNotRenewableException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-005}.
     *
     * @param contractNumber número del contrato
     * @param reason por qué no se puede renovar
     */
    public ContractNotRenewableException(String contractNumber, String reason) {
        super("CTR-005", "Contract %s cannot be renewed: %s".formatted(contractNumber, reason));
    }
}
