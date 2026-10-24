package pe.edu.galaxy.training.ia.ca.application.exception;

import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;

public class DuplicateEmployeeDocumentException extends ConflictException {

    public DuplicateEmployeeDocumentException(DocumentNumber documentNumber) {
        super("EMP-001", "An employee with document %s %s already exists."
                .formatted(documentNumber.type(), documentNumber.value()));
    }
}
