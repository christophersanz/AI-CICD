package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

public record ContractId(UUID value) implements Identifier {

    public ContractId {
        if (value == null) {
            throw new InvalidValueException("ContractId value is required.");
        }
    }

    public static ContractId generate() {
        return new ContractId(UUID.randomUUID());
    }

    public static ContractId of(UUID value) {
        return new ContractId(value);
    }

    public static ContractId of(String value) {
        if (value == null) {
            throw new InvalidValueException("ContractId value is required.");
        }
        try {
            return new ContractId(UUID.fromString(value.strip()));
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("ContractId must be a valid UUID: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
