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

    /** Crea un contrato en borrador validando BR-CTR-002, BR-CTR-003 y BR-CTR-004. */
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
     * Reconstruye un contrato ya persistido, sin volver a emitir eventos ni validar reglas de negocio:
     * se asume que el estado persistido ya las cumplía. Uso exclusivo de los adaptadores de persistencia.
     *
     * @param termination {@code null} si el contrato no fue terminado
     */
    public static Contract restore(ContractId id, ContractNumber number, EmployeeId employeeId, ContractType type,
                                   JobTitle jobTitle, Salary salary, ContractPeriod period, ContractStatus status,
                                   List<ContractRenewal> renewals, Termination termination) {
        return new Contract(id, number, employeeId, type, jobTitle, salary, period, status, renewals, termination);
    }

    /** Modifica los datos del contrato (BR-CTR-006, BR-CTR-003, BR-CTR-004). */
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

    public void sign(Clock clock) {
        transitionTo(ContractStatus.ACTIVE, "sign");
        registerEvent(new ContractSigned(newEventId(), now(clock), id(), period.startDate()));
    }

    public void cancel() {
        transitionTo(ContractStatus.CANCELLED, "cancel");
    }

    /** Extiende la fecha de fin de un contrato a plazo fijo vigente (BR-CTR-007). */
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

    /** Termina anticipadamente un contrato vigente (BR-CTR-009). */
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

    public boolean isExpirableAt(LocalDate cutoffDate) {
        Guard.notNull(cutoffDate, "cutoffDate");
        return type.isExpirable()
                && status.canTransitionTo(ContractStatus.EXPIRED)
                && cutoffDate.isAfter(currentEndDate());
    }

    /** Marca como vencido un contrato a plazo fijo cuya fecha de fin ya pasó. */
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

    public ContractNumber number() {
        return number;
    }

    public EmployeeId employeeId() {
        return employeeId;
    }

    public ContractType type() {
        return type;
    }

    public JobTitle jobTitle() {
        return jobTitle;
    }

    public Salary salary() {
        return salary;
    }

    public ContractPeriod period() {
        return period;
    }

    public ContractStatus status() {
        return status;
    }

    public List<ContractRenewal> renewals() {
        return List.copyOf(renewals);
    }

    public Optional<Termination> termination() {
        return Optional.ofNullable(termination);
    }
}
