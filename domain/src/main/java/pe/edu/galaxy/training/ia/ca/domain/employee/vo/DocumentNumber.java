package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException;

/** Documento de identidad del empleado; el formato lo define su {@link DocumentType} (BR-EMP-002). */
public record DocumentNumber(DocumentType type, String value) {

    public DocumentNumber {
        if (type == null) {
            throw new InvalidDocumentNumberException("Document type is required.");
        }
        value = value == null ? "" : value.strip();
        if (value.isEmpty()) {
            throw new InvalidDocumentNumberException("Document number is required.");
        }
        if (!type.isValidFormat(value)) {
            throw new InvalidDocumentNumberException(type.formatDescription());
        }
    }

    public static DocumentNumber of(DocumentType type, String value) {
        return new DocumentNumber(type, value);
    }
}
