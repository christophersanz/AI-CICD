package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * La operación no está permitida en el estado actual del contrato: es una transición que no aparece en el diagrama de
 * business-rules §5 ({@code CTR-008}, HTTP 409).
 */
public class InvalidContractStatusTransitionException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-008}.
     *
     * @param contractNumber número del contrato
     * @param status estado actual
     * @param operation operación rechazada, por ejemplo {@code sign} o {@code expire}
     */
    public InvalidContractStatusTransitionException(String contractNumber, String status, String operation) {
        super("CTR-008", "Operation '%s' is not allowed for contract %s in status %s."
                .formatted(operation, contractNumber, status));
    }
}
