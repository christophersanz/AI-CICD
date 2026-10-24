package pe.edu.galaxy.training.ia.ca.application.dto;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

/**
 * Criterios del listado de empleados (BR-EMP-004). Los filtros son opcionales y los textos en blanco se
 * tratan como ausentes. Se busca por documento (tipo y número juntos) o por nombre, nunca por ambos.
 *
 * @param name texto a buscar, parcial y sin distinguir mayúsculas ni tildes, en nombres y apellidos
 * @param page página, desde 0
 */
public record EmployeeSearchQuery(
        DocumentType documentType,
        String documentNumber,
        String name,
        int page,
        int size,
        EmployeeSortField sortField,
        SortDirection direction) {

    public static final int MAX_PAGE_SIZE = 100;

    public EmployeeSearchQuery {
        documentNumber = blankToNull(documentNumber);
        name = blankToNull(name);
        if ((documentType == null) != (documentNumber == null)) {
            throw new InvalidValueException("Document type and number must be provided together.");
        }
        if (documentNumber != null && name != null) {
            throw new InvalidValueException("Search by document or by name, not both.");
        }
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidValueException("Page must be >= 0 and size between 1 and %d.".formatted(MAX_PAGE_SIZE));
        }
        Guard.notNull(sortField, "sortField");
        Guard.notNull(direction, "direction");
    }

    public boolean hasDocumentFilter() {
        return documentType != null;
    }

    public boolean hasNameFilter() {
        return name != null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
