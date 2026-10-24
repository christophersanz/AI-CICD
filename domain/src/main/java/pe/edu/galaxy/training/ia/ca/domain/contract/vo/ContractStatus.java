package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.util.Set;

/**
 * Estado del contrato y sus transiciones (business-rules.md §5).
 *
 * <p>{@link #canTransitionTo} solo cubre los cambios de estado. Las operaciones que no cambian el
 * estado ({@code update()} en {@code DRAFT}, {@code renew()} en {@code ACTIVE}) se validan con
 * {@link #isEditable()} y con la regla específica BR-CTR-007.
 */
public enum ContractStatus {

    DRAFT,
    ACTIVE,
    TERMINATED,
    EXPIRED,
    CANCELLED;

    public boolean canTransitionTo(ContractStatus target) {
        return nextStatuses().contains(target);
    }

    /** BR-CTR-006: solo los contratos en borrador pueden modificarse. */
    public boolean isEditable() {
        return this == DRAFT;
    }

    public boolean isFinal() {
        return nextStatuses().isEmpty();
    }

    private Set<ContractStatus> nextStatuses() {
        return switch (this) {
            case DRAFT -> Set.of(ACTIVE, CANCELLED);
            case ACTIVE -> Set.of(TERMINATED, EXPIRED);
            case TERMINATED, EXPIRED, CANCELLED -> Set.of();
        };
    }
}
