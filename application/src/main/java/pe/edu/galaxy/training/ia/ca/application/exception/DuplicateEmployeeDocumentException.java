package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;

/** Ya existe un empleado con ese tipo y número de documento (BR-EMP-001, {@code EMP-001}, HTTP 409). */
public class DuplicateEmployeeDocumentException extends ConflictException {

    /**
     * Crea el error con el código {@code EMP-001}.
     *
     * @param documentNumber documento repetido
     */
    public DuplicateEmployeeDocumentException(DocumentNumber documentNumber) {
        super("EMP-001", "An employee with document %s %s already exists."
                .formatted(documentNumber.type(), documentNumber.value()));
    }
}
