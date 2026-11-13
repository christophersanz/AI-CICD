package pe.edu.galaxy.training.ia.ca.application.dto;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

/**
 * Criterios del listado de empleados (BR-EMP-004). Los filtros son opcionales y los textos en blanco se
 * tratan como ausentes. Se busca por documento (tipo y número juntos) o por nombre, nunca por ambos.
 *
 * @param documentType tipo de documento; va junto con {@code documentNumber}
 * @param documentNumber número de documento, búsqueda exacta
 * @param name texto a buscar, parcial y sin distinguir mayúsculas ni tildes, en nombres y apellidos
 * @param page página, desde 0
 * @param size tamaño de página, de 1 a {@link #MAX_PAGE_SIZE}
 * @param sortField campo de orden
 * @param direction sentido del orden
 */
public record EmployeeSearchQuery(
        DocumentType documentType,
        String documentNumber,
        String name,
        int page,
        int size,
        EmployeeSortField sortField,
        SortDirection direction) {

    /** Tamaño máximo de página, para acotar el costo de cada consulta. */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Trata los textos en blanco como ausentes y valida que los filtros no se combinen (BR-EMP-004).
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si el documento viene sin tipo
     *         o sin número, se combina con el nombre, la página o el tamaño están fuera de rango, o falta el orden
     *         ({@code VAL-001})
     */
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

    /** {@return {@code true} si se busca por documento} */
    public boolean hasDocumentFilter() {
        return documentType != null;
    }

    /** {@return {@code true} si se busca por nombre} */
    public boolean hasNameFilter() {
        return name != null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
