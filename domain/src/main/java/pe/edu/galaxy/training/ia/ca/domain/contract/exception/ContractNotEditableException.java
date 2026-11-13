package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/** Se intentó modificar un contrato que ya no está en {@code DRAFT} (BR-CTR-006, {@code CTR-004}, HTTP 422). */
public class ContractNotEditableException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-004}.
     *
     * @param contractNumber número del contrato
     * @param status estado actual que impide modificarlo
     */
    public ContractNotEditableException(String contractNumber, String status) {
        super("CTR-004", "Contract %s cannot be modified because it is %s.".formatted(contractNumber, status));
    }
}
