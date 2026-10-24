package pe.edu.galaxy.training.ia.ca.application.port.out;

import java.util.Optional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

public interface EmployeeRepositoryPort {

    boolean existsByDocumentNumber(DocumentNumber documentNumber);

    Optional<Employee> findById(EmployeeId employeeId);

    /**
     * Busca empleados según los criterios (BR-EMP-004): documento exacto (tipo + número) y nombre parcial,
     * sin distinguir mayúsculas ni tildes, en nombres y apellidos. El orden es estable entre páginas.
     */
    PageResult<Employee> search(EmployeeSearchQuery query);

    /**
     * Persiste el empleado.
     *
     * @throws pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException
     *         si otro registro con el mismo documento se guardó concurrentemente
     */
    Employee save(Employee employee);
}
