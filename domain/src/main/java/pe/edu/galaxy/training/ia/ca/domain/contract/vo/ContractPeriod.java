package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException;

/**
 * Vigencia del contrato: fecha de inicio y, para {@code FIXED_TERM}, fecha de fin
 * (BR-CTR-002 y BR-CTR-003).
 *
 * <p>Es una clase y no un {@code record} para no exponer la fecha de fin como componente
 * {@code null}: se accede con {@link #endDate()}, que devuelve {@code Optional}.
 */
public final class ContractPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    private ContractPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static ContractPeriod indefinite(LocalDate startDate) {
        return of(ContractType.INDEFINITE, startDate, null);
    }

    public static ContractPeriod fixedTerm(LocalDate startDate, LocalDate endDate) {
        return of(ContractType.FIXED_TERM, startDate, endDate);
    }

    /**
     * Construye la vigencia validándola contra el tipo de contrato.
     *
     * @param endDate fecha de fin; debe ser {@code null} si el tipo no la requiere
     */
    public static ContractPeriod of(ContractType type, LocalDate startDate, LocalDate endDate) {
        Guard.notNull(type, "Contract type");
        if (startDate == null) {
            throw new InvalidContractPeriodException("Contract start date is required.");
        }
        if (!type.requiresEndDate() && endDate != null) {
            throw new InvalidContractPeriodException("A %s contract cannot have an end date.".formatted(type));
        }
        if (type.requiresEndDate() && (endDate == null || !endDate.isAfter(startDate))) {
            throw new InvalidContractPeriodException(
                    "A %s contract requires an end date after the start date.".formatted(type));
        }
        return new ContractPeriod(startDate, endDate);
    }

    public LocalDate startDate() {
        return startDate;
    }

    public Optional<LocalDate> endDate() {
        return Optional.ofNullable(endDate);
    }

    public boolean isOpenEnded() {
        return endDate == null;
    }

    public boolean isCompatibleWith(ContractType type) {
        return type.requiresEndDate() != isOpenEnded();
    }

    /** Fechas inclusivas en ambos extremos (BR-CTR-009). */
    public boolean contains(LocalDate date) {
        Guard.notNull(date, "date");
        return !date.isBefore(startDate) && (isOpenEnded() || !date.isAfter(endDate));
    }

    /** Dos vigencias se superponen si comparten al menos un día (BR-CTR-005). */
    public boolean overlaps(ContractPeriod other) {
        Guard.notNull(other, "other period");
        boolean startsBeforeOtherEnds = other.isOpenEnded() || !startDate.isAfter(other.endDate);
        boolean otherStartsBeforeThisEnds = isOpenEnded() || !other.startDate.isAfter(endDate);
        return startsBeforeOtherEnds && otherStartsBeforeThisEnds;
    }

    /**
     * Duración inclusiva de la vigencia, base para acumular plazos fijos (BR-CTR-008).
     * Vacía si la vigencia no tiene fecha de fin.
     */
    public Optional<Period> duration() {
        return endDate().map(end -> Period.between(startDate, end.plusDays(1)));
    }

    /**
     * Extiende la fecha de fin. Pública porque la usa el agregado desde otro paquete; exige una
     * vigencia con fecha de fin y una nueva fecha posterior a la actual. Las reglas de estado y tipo
     * de BR-CTR-007 las valida {@code Contract.renew()}.
     */
    public ContractPeriod extendTo(LocalDate newEndDate) {
        if (isOpenEnded()) {
            throw new InvalidContractPeriodException("An open-ended period cannot be extended.");
        }
        if (newEndDate == null || !newEndDate.isAfter(endDate)) {
            throw new InvalidContractPeriodException(
                    "The new end date must be after the current end date %s.".formatted(endDate));
        }
        return new ContractPeriod(startDate, newEndDate);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ContractPeriod other
                && startDate.equals(other.startDate)
                && Objects.equals(endDate, other.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "ContractPeriod[" + startDate + " - " + (isOpenEnded() ? "indefinite" : endDate) + "]";
    }
}
