package pe.edu.galaxy.training.ia.ca.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.UpdateEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

/** Actualiza nombres y correo de un empleado existente; el documento no cambia (BR-EMP-003). */
@Transactional
public class UpdateEmployeeService implements UpdateEmployeeUseCase {

    private final EmployeeRepositoryPort employeeRepository;

    public UpdateEmployeeService(EmployeeRepositoryPort employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResult update(UpdateEmployeeCommand command) {
        EmployeeId employeeId = new EmployeeId(command.employeeId());
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        employee.update(new FullName(command.firstNames(), command.lastNames()), new Email(command.email()));
        return EmployeeResult.from(employeeRepository.save(employee));
    }
}
