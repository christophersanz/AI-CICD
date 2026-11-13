package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Optional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

/** Persistencia del agregado {@code Employee}. La implementa {@code infrastructure/persistence}. */
public interface EmployeeRepositoryPort {

    /**
     * Indica si ya hay un empleado con ese documento, comparando tipo y número (BR-EMP-001).
     * @param documentNumber documento ya validado por el dominio
     * @return {@code true} si existe un empleado con ese documento
     */
    boolean existsByDocumentNumber(DocumentNumber documentNumber);

    /**
     * Carga el agregado completo.
     * @param employeeId identificador del empleado
     * @return el empleado, o vacío si no existe
     */
    Optional<Employee> findById(EmployeeId employeeId);

    /**
     * Busca empleados según los criterios (BR-EMP-004): documento exacto (tipo + número) y nombre parcial,
     * sin distinguir mayúsculas ni tildes, en nombres y apellidos. El orden es estable entre páginas.
     *
     * @param query filtros, paginación y orden
     * @return la página pedida con el total de coincidencias
     */
    PageResult<Employee> search(EmployeeSearchQuery query);

    /**
     * Inserta el empleado o actualiza el existente con el mismo identificador.
     *
     * @param employee el agregado a guardar
     * @return el empleado tal como quedó guardado
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException
     *         si otro registro con el mismo documento se guardó concurrentemente
     */
    Employee save(Employee employee);
}
