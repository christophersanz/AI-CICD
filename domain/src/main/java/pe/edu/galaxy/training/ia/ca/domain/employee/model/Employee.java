package pe.edu.galaxy.training.ia.ca.domain.employee.model;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.model.AggregateRoot;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

/** Persona contratada. Raíz del agregado. */
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

    public static Employee register(DocumentNumber documentNumber, FullName fullName, Email email) {
        return new Employee(EmployeeId.generate(), documentNumber, fullName, email);
    }

    public static Employee restore(EmployeeId id, DocumentNumber documentNumber, FullName fullName, Email email) {
        return new Employee(id, documentNumber, fullName, email);
    }

    /** Reemplaza los datos personales; el documento de identidad no se modifica. */
    public void update(FullName fullName, Email email) {
        Guard.notNull(fullName, "fullName");
        Guard.notNull(email, "email");
        this.fullName = fullName;
        this.email = email;
    }

    public DocumentNumber documentNumber() {
        return documentNumber;
    }

    public FullName fullName() {
        return fullName;
    }

    public Email email() {
        return email;
    }
}
