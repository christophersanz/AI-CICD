package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException;

/**
 * Documento de identidad del empleado; el formato lo define su {@link DocumentType} (BR-EMP-002). Es único por tipo y
 * número (BR-EMP-001).
 *
 * @param type tipo de documento
 * @param value número sin espacios en los extremos
 */
public record DocumentNumber(DocumentType type, String value) {

    /**
     * Quita los espacios del número y valida el formato de su tipo.
     * @throws pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException si falta el tipo o
     *         el número, o el número no cumple el formato ({@code EMP-002})
     */
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

    /**
     * Crea el documento validando su formato.
     * @param type tipo de documento
     * @param value número, por ejemplo {@code 12345678} para un DNI
     * @return el documento
     * @throws pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException si no es válido
     *         ({@code EMP-002})
     */
    public static DocumentNumber of(DocumentType type, String value) {
        return new DocumentNumber(type, value);
    }
}
