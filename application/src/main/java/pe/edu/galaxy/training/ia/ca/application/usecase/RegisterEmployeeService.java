package pe.edu.galaxy.training.ia.ca.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.RegisterEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException;
import pe.edu.galaxy.training.ia.ca.application.port.in.RegisterEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

/** Registra un empleado (BR-EMP-001, BR-EMP-002). */
@Transactional
public class RegisterEmployeeService implements RegisterEmployeeUseCase {

    private final EmployeeRepositoryPort employeeRepository;

    public RegisterEmployeeService(EmployeeRepositoryPort employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResult register(RegisterEmployeeCommand command) {
        DocumentNumber documentNumber = new DocumentNumber(command.documentType(), command.documentNumber());
        if (employeeRepository.existsByDocumentNumber(documentNumber)) {
            throw new DuplicateEmployeeDocumentException(documentNumber);
        }
        Employee employee = Employee.register(
                documentNumber,
                new FullName(command.firstNames(), command.lastNames()),
                new Email(command.email()));
        return EmployeeResult.from(employeeRepository.save(employee));
    }
}
