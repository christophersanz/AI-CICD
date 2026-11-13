package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;

/**
 * Lista empleados paginados (actor: quien tenga {@code employee:read}). Se filtra por documento, de forma exacta, o
 * por nombre, de forma parcial y sin distinguir mayúsculas ni tildes; los dos filtros no se combinan (BR-EMP-004).
 */
@FunctionalInterface
public interface ListEmployeesUseCase {

    /**
     * Devuelve una página de empleados que cumplen el filtro.
     *
     * @param query filtros, página, tamaño y orden; la exclusión entre documento y nombre ya se validó al crearla
     * @return la página pedida y los totales; vacía si nada coincide
     */
    PageResult<EmployeeResult> list(EmployeeSearchQuery query);
}
