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

    /**
     * Crea la vigencia de un contrato {@code INDEFINITE}, sin fecha de fin.
     * @param startDate fecha de inicio
     * @return la vigencia abierta
     * @throws pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException si falta la fecha
     *         de inicio (BR-CTR-002, {@code CTR-001})
     */
    public static ContractPeriod indefinite(LocalDate startDate) {
        return of(ContractType.INDEFINITE, startDate, null);
    }

    /**
     * Crea la vigencia de un contrato {@code FIXED_TERM}.
     * @param startDate fecha de inicio
     * @param endDate fecha de fin, posterior a la de inicio
     * @return la vigencia con fin
     * @throws pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException si falta una fecha
     *         o el fin no es posterior al inicio (BR-CTR-002, BR-CTR-003, {@code CTR-001})
     */
    public static ContractPeriod fixedTerm(LocalDate startDate, LocalDate endDate) {
        return of(ContractType.FIXED_TERM, startDate, endDate);
    }

    /**
     * Construye la vigencia validándola contra el tipo de contrato (BR-CTR-002, BR-CTR-003).
     *
     * @param type tipo de contrato, que decide si hay fecha de fin
     * @param startDate fecha de inicio, obligatoria
     * @param endDate fecha de fin; debe ser {@code null} si el tipo no la requiere
     * @return la vigencia validada
     * @throws InvalidContractPeriodException si falta el inicio, un {@code INDEFINITE} trae fin o un {@code FIXED_TERM}
     *         no tiene fin posterior al inicio ({@code CTR-001})
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta el tipo
     *         ({@code VAL-001})
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

    /** {@return la fecha de inicio} */
    public LocalDate startDate() {
        return startDate;
    }

    /** {@return la fecha de fin, o vacío si el contrato es {@code INDEFINITE}} */
    public Optional<LocalDate> endDate() {
        return Optional.ofNullable(endDate);
    }

    /** {@return {@code true} si la vigencia no tiene fecha de fin} */
    public boolean isOpenEnded() {
        return endDate == null;
    }

    /**
     * Indica si la vigencia corresponde al tipo: con fin para {@code FIXED_TERM} y sin fin para {@code INDEFINITE}.
     * @param type tipo de contrato
     * @return {@code true} si la vigencia es válida para ese tipo
     */
    public boolean isCompatibleWith(ContractType type) {
        return type.requiresEndDate() != isOpenEnded();
    }

    /**
     * Indica si una fecha cae dentro de la vigencia, con ambos extremos incluidos (BR-CTR-009).
     *
     * @param date fecha a comprobar
     * @return {@code true} si la fecha está entre el inicio y el fin, o después del inicio si no hay fin
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta la fecha
     *         ({@code VAL-001})
     */
    public boolean contains(LocalDate date) {
        Guard.notNull(date, "date");
        return !date.isBefore(startDate) && (isOpenEnded() || !date.isAfter(endDate));
    }

    /**
     * Indica si dos vigencias comparten al menos un día (BR-CTR-005). Es simétrico y trata una vigencia sin fin como
     * infinita hacia adelante.
     *
     * @param other la otra vigencia
     * @return {@code true} si se superponen, aunque solo sea en un día
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta la otra vigencia
     *         ({@code VAL-001})
     */
    public boolean overlaps(ContractPeriod other) {
        Guard.notNull(other, "other period");
        boolean startsBeforeOtherEnds = other.isOpenEnded() || !startDate.isAfter(other.endDate);
        boolean otherStartsBeforeThisEnds = isOpenEnded() || !other.startDate.isAfter(endDate);
        return startsBeforeOtherEnds && otherStartsBeforeThisEnds;
    }

    /**
     * Calcula la duración contando el día de inicio y el de fin; es la base para acumular plazos fijos
     * (BR-CTR-008). Por ejemplo, del 1 de enero al 30 de junio es {@code P6M}.
     *
     * @return la duración, o vacío si la vigencia no tiene fecha de fin
     */
    public Optional<Period> duration() {
        return endDate().map(end -> Period.between(startDate, end.plusDays(1)));
    }

    /**
     * Extiende la fecha de fin. Pública porque la usa el agregado desde otro paquete; exige una
     * vigencia con fecha de fin y una nueva fecha posterior a la actual. Las reglas de estado y tipo
     * de BR-CTR-007 las valida {@code Contract.renew()}.
     *
     * @param newEndDate fecha de fin nueva
     * @return una vigencia nueva con el mismo inicio y la nueva fecha de fin; la actual no cambia
     * @throws InvalidContractPeriodException si la vigencia no tiene fin, o si la fecha falta o no es posterior a la
     *         actual ({@code CTR-001})
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
