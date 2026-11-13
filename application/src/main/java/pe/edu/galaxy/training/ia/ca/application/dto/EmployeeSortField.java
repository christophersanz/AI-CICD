package pe.edu.galaxy.training.ia.ca.application.dto;

/** Campos por los que se puede ordenar el listado de empleados. */
public enum EmployeeSortField {
    /** Por apellidos; es el orden por defecto (business-rules §6). */
    LAST_NAMES,
    /** Por nombres. */
    FIRST_NAMES,
    /** Por número de documento. */
    DOCUMENT_NUMBER,
    /** Por correo. */
    EMAIL
}
