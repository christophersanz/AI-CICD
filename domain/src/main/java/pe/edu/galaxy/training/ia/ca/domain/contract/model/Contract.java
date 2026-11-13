package pe.edu.galaxy.training.ia.ca.domain.contract.model;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.model.AggregateRoot;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractCreated;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractExpired;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractRenewed;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractSigned;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractTerminated;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.ContractNotEditableException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.ContractNotRenewableException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InconsistentContractStateException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractStatusTransitionException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.SalaryBelowMinimumWageException;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractNumber;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractPeriod;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractRenewal;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractType;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.JobTitle;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.MinimumWage;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.Salary;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.Termination;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

/**
 * Contrato laboral. Raíz del agregado.
 *
 * <p>Los métodos que emiten eventos reciben un {@link Clock} para fechar el evento sin que el
 * dominio consulte la hora del sistema.
 */
public final class Contract extends AggregateRoot<ContractId> {

    private final ContractNumber number;
    private final EmployeeId employeeId;
    private ContractType type;
    private JobTitle jobTitle;
    private Salary salary;
    private ContractPeriod period;
    private ContractStatus status;
    private final List<ContractRenewal> renewals;
    private Termination termination;

    private Contract(ContractId id, ContractNumber number, EmployeeId employeeId, ContractType type,
                     JobTitle jobTitle, Salary salary, ContractPeriod period, ContractStatus status,
                     List<ContractRenewal> renewals, Termination termination) {
        super(id);
        this.number = Guard.notNull(number, "number");
        this.employeeId = Guard.notNull(employeeId, "employeeId");
        this.type = Guard.notNull(type, "type");
        this.jobTitle = Guard.notNull(jobTitle, "jobTitle");
        this.salary = Guard.notNull(salary, "salary");
        this.period = Guard.notNull(period, "period");
        this.status = Guard.notNull(status, "status");
        this.renewals = new ArrayList<>(Guard.notNull(renewals, "renewals"));
        this.termination = termination;
    }

    /**
     * Crea un contrato en {@code DRAFT} y emite {@code ContractCreated}. Valida la vigencia frente al tipo (BR-CTR-002,
     * BR-CTR-003) y la remuneración frente a la RMV (BR-CTR-004). No comprueba que el empleado exista ni que no haya
     * contratos superpuestos: eso lo hace el caso de uso (BR-CTR-001, BR-CTR-005).
     *
     * @param number número generado por el sistema (BR-CTR-010)
     * @param employeeId empleado del contrato
     * @param type tipo de contrato
     * @param jobTitle cargo
     * @param salary remuneración mensual
     * @param period vigencia, que debe corresponder al tipo
     * @param minimumWage RMV vigente, con la que se compara la remuneración
     * @param clock reloj con el que se fecha el evento
     * @return el contrato nuevo, con su evento pendiente de publicar
     * @throws InvalidContractPeriodException si la vigencia no corresponde al tipo ({@code CTR-001})
     * @throws SalaryBelowMinimumWageException si la remuneración es menor que la RMV ({@code CTR-002})
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta algún dato
     *         obligatorio ({@code VAL-001})
     */
    public static Contract create(ContractNumber number, EmployeeId employeeId, ContractType type,
                                  JobTitle jobTitle, Salary salary, ContractPeriod period,
                                  MinimumWage minimumWage, Clock clock) {
        ensurePeriodMatchesType(period, type);
        ensureSalaryNotBelowMinimumWage(salary, minimumWage);
        Contract contract = new Contract(ContractId.generate(), number, employeeId, type, jobTitle, salary, period,
                ContractStatus.DRAFT, List.of(), null);
        contract.registerEvent(new ContractCreated(newEventId(), now(clock), contract.id(), employeeId, type));
        return contract;
    }

    /**
     * Reconstruye un contrato ya persistido, sin volver a emitir eventos. Uso exclusivo de los adaptadores de
     * persistencia.
     *
     * <p>Valida las invariantes estructurales del agregado (periodo compatible con el tipo, término solo en
     * {@code TERMINATED}, renovaciones solo en contratos renovables) para que un dato corrupto falle aquí y no
     * más adelante. No revalida BR-CTR-004: la RMV vigente puede ser mayor que la de la fecha del contrato.
     *
     * @param id identificador persistido
     * @param number número del contrato
     * @param employeeId empleado del contrato
     * @param type tipo de contrato
     * @param jobTitle cargo
     * @param salary remuneración mensual
     * @param period vigencia, con las renovaciones ya aplicadas
     * @param status estado persistido
     * @param renewals historial de renovaciones, en orden
     * @param termination {@code null} si el contrato no fue terminado
     * @return el contrato reconstruido, sin eventos pendientes
     * @throws InvalidContractPeriodException     si el periodo no corresponde al tipo (CTR-001)
     * @throws InconsistentContractStateException si el estado, el término o las renovaciones no concuerdan (CTR-009)
     */
    public static Contract restore(ContractId id, ContractNumber number, EmployeeId employeeId, ContractType type,
                                   JobTitle jobTitle, Salary salary, ContractPeriod period, ContractStatus status,
                                   List<ContractRenewal> renewals, Termination termination) {
        Guard.notNull(number, "number");
        Guard.notNull(status, "status");
        Guard.notNull(renewals, "renewals");
        ensurePeriodMatchesType(period, type);
        if ((status == ContractStatus.TERMINATED) != (termination != null)) {
            throw new InconsistentContractStateException(number.value(), termination == null
                    ? "status TERMINATED without termination."
                    : "termination present but status is " + status + ".");
        }
        if (!renewals.isEmpty() && !type.isRenewable()) {
            throw new InconsistentContractStateException(number.value(), "renewals present on a " + type + " contract.");
        }
        return new Contract(id, number, employeeId, type, jobTitle, salary, period, status, renewals, termination);
    }

    /**
     * Reemplaza tipo, cargo, remuneración y vigencia de un contrato en {@code DRAFT} (BR-CTR-006), con las mismas
     * validaciones que al crearlo (BR-CTR-003, BR-CTR-004). No emite eventos.
     *
     * @param newType tipo nuevo
     * @param newJobTitle cargo nuevo
     * @param newSalary remuneración nueva
     * @param newPeriod vigencia nueva, que debe corresponder al tipo nuevo
     * @param minimumWage RMV vigente
     * @throws ContractNotEditableException si el contrato ya no está en {@code DRAFT} ({@code CTR-004})
     * @throws InvalidContractPeriodException si la vigencia no corresponde al tipo ({@code CTR-001})
     * @throws SalaryBelowMinimumWageException si la remuneración es menor que la RMV ({@code CTR-002})
     */
    public void update(ContractType newType, JobTitle newJobTitle, Salary newSalary, ContractPeriod newPeriod,
                       MinimumWage minimumWage) {
        if (!status.isEditable()) {
            throw new ContractNotEditableException(number.value(), status.name());
        }
        ensurePeriodMatchesType(newPeriod, newType);
        ensureSalaryNotBelowMinimumWage(newSalary, minimumWage);
        this.type = newType;
        this.jobTitle = Guard.notNull(newJobTitle, "jobTitle");
        this.salary = newSalary;
        this.period = newPeriod;
    }

    /**
     * Firma el contrato: pasa de {@code DRAFT} a {@code ACTIVE} y emite {@code ContractSigned}. La comprobación de que
     * no haya otro contrato activo superpuesto la hace el caso de uso (BR-CTR-005).
     * @param clock reloj con el que se fecha el evento
     * @throws InvalidContractStatusTransitionException si el contrato no está en {@code DRAFT} ({@code CTR-008})
     */
    public void sign(Clock clock) {
        transitionTo(ContractStatus.ACTIVE, "sign");
        registerEvent(new ContractSigned(newEventId(), now(clock), id(), period.startDate()));
    }

    /**
     * Cancela un contrato que nunca se firmó: pasa de {@code DRAFT} a {@code CANCELLED}. No emite eventos.
     * @throws InvalidContractStatusTransitionException si el contrato no está en {@code DRAFT} ({@code CTR-008})
     */
    public void cancel() {
        transitionTo(ContractStatus.CANCELLED, "cancel");
    }

    /**
     * Extiende la fecha de fin de un contrato {@code FIXED_TERM} en {@code ACTIVE}, registra la renovación en el
     * historial y emite {@code ContractRenewed} (BR-CTR-007). El límite de 5 años acumulados (BR-CTR-008) lo comprueba
     * el caso de uso.
     *
     * @param newEndDate fecha de fin nueva, posterior a la actual
     * @param clock reloj con el que se fecha el evento
     * @throws ContractNotRenewableException si el contrato no es {@code FIXED_TERM} en {@code ACTIVE}, o si la fecha
     *         falta o no es posterior a la actual ({@code CTR-005})
     */
    public void renew(LocalDate newEndDate, Clock clock) {
        if (!type.isRenewable() || status != ContractStatus.ACTIVE) {
            throw new ContractNotRenewableException(number.value(), "only ACTIVE FIXED_TERM contracts can be renewed.");
        }
        LocalDate currentEndDate = currentEndDate();
        if (newEndDate == null || !newEndDate.isAfter(currentEndDate)) {
            throw new ContractNotRenewableException(number.value(),
                    "the new end date must be after " + currentEndDate + ".");
        }
        renewals.add(new ContractRenewal(currentEndDate, newEndDate));
        period = period.extendTo(newEndDate);
        registerEvent(new ContractRenewed(newEventId(), now(clock), id(), currentEndDate, newEndDate));
    }

    /**
     * Termina anticipadamente un contrato {@code ACTIVE}: guarda el término, pasa a {@code TERMINATED} y emite
     * {@code ContractTerminated} (BR-CTR-009).
     *
     * @param terminationDate fecha del término, dentro de la vigencia (ambos extremos incluidos)
     * @param reason motivo, obligatorio
     * @param clock reloj con el que se fecha el evento
     * @throws InvalidContractStatusTransitionException si el contrato no está en {@code ACTIVE} ({@code CTR-008})
     * @throws InvalidTerminationException si falta el motivo o la fecha, o la fecha está fuera de la vigencia
     *         ({@code CTR-007})
     */
    public void terminate(LocalDate terminationDate, String reason, Clock clock) {
        ensureCanTransitionTo(ContractStatus.TERMINATED, "terminate");
        Termination newTermination = new Termination(terminationDate, reason);
        if (!period.contains(newTermination.date())) {
            throw new InvalidTerminationException(
                    "Termination date %s must be within the contract period %s.".formatted(terminationDate, period));
        }
        termination = newTermination;
        status = ContractStatus.TERMINATED;
        registerEvent(new ContractTerminated(newEventId(), now(clock), id(), newTermination.date(),
                newTermination.reason()));
    }

    /**
     * Indica si el contrato debe vencer en la fecha de corte: es {@code FIXED_TERM}, está {@code ACTIVE} y su fecha de
     * fin es anterior a la de corte (business-rules §5). Lo usa el Sistema para elegir qué contratos vencer.
     * @param cutoffDate fecha de corte del proceso de vencimiento
     * @return {@code true} si {@link #expire(LocalDate, Clock)} lo aceptaría
     */
    public boolean isExpirableAt(LocalDate cutoffDate) {
        Guard.notNull(cutoffDate, "cutoffDate");
        return type.isExpirable()
                && status.canTransitionTo(ContractStatus.EXPIRED)
                && cutoffDate.isAfter(currentEndDate());
    }

    /**
     * Marca como vencido un contrato a plazo fijo cuya fecha de fin ya pasó: pasa a {@code EXPIRED} y emite
     * {@code ContractExpired}.
     *
     * @param cutoffDate fecha de corte; la fecha de fin debe ser anterior a ella
     * @param clock reloj con el que se fecha el evento
     * @throws InvalidContractStatusTransitionException si {@link #isExpirableAt(LocalDate)} es {@code false} ({@code
     *         CTR-008})
     */
    public void expire(LocalDate cutoffDate, Clock clock) {
        if (!isExpirableAt(cutoffDate)) {
            throw new InvalidContractStatusTransitionException(number.value(), status.name(), "expire");
        }
        status = ContractStatus.EXPIRED;
        registerEvent(new ContractExpired(newEventId(), now(clock), id(), currentEndDate()));
    }

    private void transitionTo(ContractStatus target, String operation) {
        ensureCanTransitionTo(target, operation);
        status = target;
    }

    private void ensureCanTransitionTo(ContractStatus target, String operation) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidContractStatusTransitionException(number.value(), status.name(), operation);
        }
    }

    private LocalDate currentEndDate() {
        return period.endDate().orElseThrow(() -> new InvalidContractPeriodException(
                "Contract %s has no end date.".formatted(number)));
    }

    private static void ensurePeriodMatchesType(ContractPeriod period, ContractType type) {
        Guard.notNull(type, "type");
        Guard.notNull(period, "period");
        if (!period.isCompatibleWith(type)) {
            throw new InvalidContractPeriodException("Contract period %s is not valid for a %s contract."
                    .formatted(period, type));
        }
    }

    private static void ensureSalaryNotBelowMinimumWage(Salary salary, MinimumWage minimumWage) {
        Guard.notNull(salary, "salary");
        Guard.notNull(minimumWage, "minimumWage");
        if (salary.isBelow(minimumWage)) {
            throw new SalaryBelowMinimumWageException(salary.amount().amount(), minimumWage.amount().amount());
        }
    }

    private static UUID newEventId() {
        return UUID.randomUUID();
    }

    private static Instant now(Clock clock) {
        return Guard.notNull(clock, "clock").instant();
    }

    /** {@return el número legible del contrato, con formato {@code CTR-AAAA-NNNNNN}} */
    public ContractNumber number() {
        return number;
    }

    /** {@return el empleado del contrato; no cambia} */
    public EmployeeId employeeId() {
        return employeeId;
    }

    /** {@return el tipo de contrato} */
    public ContractType type() {
        return type;
    }

    /** {@return el cargo} */
    public JobTitle jobTitle() {
        return jobTitle;
    }

    /** {@return la remuneración mensual} */
    public Salary salary() {
        return salary;
    }

    /** {@return la vigencia, con las renovaciones ya aplicadas} */
    public ContractPeriod period() {
        return period;
    }

    /** {@return el estado actual} */
    public ContractStatus status() {
        return status;
    }

    /** {@return una copia inmutable del historial de renovaciones, en orden} */
    public List<ContractRenewal> renewals() {
        return List.copyOf(renewals);
    }

    /** {@return el término, presente solo si el estado es {@code TERMINATED}} */
    public Optional<Termination> termination() {
        return Optional.ofNullable(termination);
    }
}
