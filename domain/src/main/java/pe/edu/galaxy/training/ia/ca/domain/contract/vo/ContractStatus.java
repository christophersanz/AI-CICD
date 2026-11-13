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

    /** Borrador: se puede modificar, firmar o cancelar. */
    DRAFT,
    /** Vigente: firmado; se puede renovar, terminar o vencer. */
    ACTIVE,
    /** Terminado anticipadamente. Estado final. */
    TERMINATED,
    /** Vencido al pasar su fecha de fin. Estado final. */
    EXPIRED,
    /** Cancelado sin haberse firmado. Estado final. */
    CANCELLED;

    /**
     * Indica si el cambio de estado aparece en el diagrama de business-rules §5.
     * @param target estado al que se quiere pasar
     * @return {@code true} si la transición está permitida
     */
    public boolean canTransitionTo(ContractStatus target) {
        return nextStatuses().contains(target);
    }

    /** {@return {@code true} solo en {@code DRAFT}, el único estado en que el contrato se modifica (BR-CTR-006)} */
    public boolean isEditable() {
        return this == DRAFT;
    }

    /** {@return {@code true} si ya no admite ninguna transición} */
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
