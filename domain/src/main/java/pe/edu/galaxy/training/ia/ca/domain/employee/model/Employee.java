package pe.edu.galaxy.training.ia.ca.domain.employee.model;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.model.AggregateRoot;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

/**
 * Persona contratada. Raíz del agregado. Hoy no emite eventos ni tiene estado: {@code EmployeeStatus} llega con
 * {@code DeactivateEmployeeUseCase} (business-rules §3).
 */
public final class Employee extends AggregateRoot<EmployeeId> {

    private final DocumentNumber documentNumber;
    private FullName fullName;
    private Email email;

    private Employee(EmployeeId id, DocumentNumber documentNumber, FullName fullName, Email email) {
        super(id);
        this.documentNumber = Guard.notNull(documentNumber, "documentNumber");
        this.fullName = Guard.notNull(fullName, "fullName");
        this.email = Guard.notNull(email, "email");
    }

    /**
     * Registra un empleado nuevo con un identificador generado. La unicidad del documento la comprueba el caso de uso
     * (BR-EMP-001).
     * @param documentNumber documento de identidad ya validado (BR-EMP-002)
     * @param fullName nombres y apellidos
     * @param email correo
     * @return el empleado nuevo
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta algún dato ({@code
     *         VAL-001})
     */
    public static Employee register(DocumentNumber documentNumber, FullName fullName, Email email) {
        return new Employee(EmployeeId.generate(), documentNumber, fullName, email);
    }

    /**
     * Reconstruye un empleado ya persistido. Uso exclusivo de los adaptadores de persistencia.
     * @param id identificador persistido
     * @param documentNumber documento de identidad
     * @param fullName nombres y apellidos
     * @param email correo
     * @return el empleado reconstruido
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta algún dato ({@code
     *         VAL-001})
     */
    public static Employee restore(EmployeeId id, DocumentNumber documentNumber, FullName fullName, Email email) {
        return new Employee(id, documentNumber, fullName, email);
    }

    /**
     * Reemplaza los nombres y el correo; el documento de identidad no se modifica (BR-EMP-003).
     *
     * @param fullName nombres y apellidos nuevos
     * @param email correo nuevo
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta alguno ({@code
     *         VAL-001})
     */
    public void update(FullName fullName, Email email) {
        Guard.notNull(fullName, "fullName");
        Guard.notNull(email, "email");
        this.fullName = fullName;
        this.email = email;
    }

    /** {@return el documento de identidad; no cambia después del registro} */
    public DocumentNumber documentNumber() {
        return documentNumber;
    }

    /** {@return los nombres y apellidos} */
    public FullName fullName() {
        return fullName;
    }

    /** {@return el correo} */
    public Email email() {
        return email;
    }
}
