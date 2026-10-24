package pe.edu.galaxy.training.ia.ca.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.ListEmployeesUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;

/** Lista empleados paginados con filtros opcionales por documento y nombre (BR-EMP-004). */
@Transactional(readOnly = true)
public class ListEmployeesService implements ListEmployeesUseCase {

    private final EmployeeRepositoryPort employeeRepository;

    public ListEmployeesService(EmployeeRepositoryPort employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public PageResult<EmployeeResult> list(EmployeeSearchQuery query) {
        return employeeRepository.search(query).map(EmployeeResult::from);
    }
}
