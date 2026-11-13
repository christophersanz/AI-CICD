package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Identificador técnico del contrato, generado al crearlo (business-rules §3). Distinto del número legible {@link
 * ContractNumber}.
 * @param value UUID del contrato
 */
public record ContractId(UUID value) implements Identifier {

    /**
     * Valida que el UUID esté presente.
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    public ContractId {
        if (value == null) {
            throw new InvalidValueException("ContractId value is required.");
        }
    }

    /** {@return un identificador nuevo y aleatorio} */
    public static ContractId generate() {
        return new ContractId(UUID.randomUUID());
    }

    /**
     * Envuelve un UUID existente, por ejemplo uno leído de la base.
     * @param value UUID del contrato
     * @return el identificador
     */
    public static ContractId of(UUID value) {
        return new ContractId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
