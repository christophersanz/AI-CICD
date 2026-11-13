package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * Un contrato persistido no cumple las invariantes estructurales del agregado (CTR-009): por ejemplo, está
 * {@code TERMINATED} sin término o tiene renovaciones sin ser renovable. Solo puede ocurrir al reconstruirlo
 * desde la base ({@code Contract.restore}); indica datos corruptos, no un error del cliente.
 */
public class InconsistentContractStateException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-009}.
     *
     * @param contractNumber número del contrato leído de la base
     * @param detail qué invariante estructural no se cumple
     */
    public InconsistentContractStateException(String contractNumber, String detail) {
        super("CTR-009", "Persisted contract %s is inconsistent: %s".formatted(contractNumber, detail));
    }
}
