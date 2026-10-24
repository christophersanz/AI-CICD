package pe.edu.galaxy.training.ia.ca.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

/** Consulta un empleado por su identificador. */
@Transactional(readOnly = true)
public class GetEmployeeService implements GetEmployeeUseCase {

    private final EmployeeRepositoryPort employeeRepository;

    public GetEmployeeService(EmployeeRepositoryPort employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResult get(EmployeeQuery query) {
        EmployeeId employeeId = new EmployeeId(query.employeeId());
        return employeeRepository.findById(employeeId)
                .map(EmployeeResult::from)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}
