package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

@ExtendWith(MockitoExtension.class)
class GetEmployeeServiceTest {

    private static final EmployeeId EMPLOYEE_ID = EmployeeId.generate();

    @Mock
    private EmployeeRepositoryPort employeeRepository;

    @InjectMocks
    private GetEmployeeService service;

    @Test
    void shouldReturnEmployeeWhenItExists() {
        // Given
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(Employee.restore(EMPLOYEE_ID,
                new DocumentNumber(DocumentType.DNI, "12345678"), new FullName("Ana", "Pérez"),
                new Email("ana@example.com"))));

        // When
        EmployeeResult result = service.get(new EmployeeQuery(EMPLOYEE_ID.value()));

        // Then
        assertThat(result.id()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(result.documentNumber()).isEqualTo("12345678");
        assertThat(result.lastNames()).isEqualTo("Pérez");
    }

    @Test
    void shouldThrowEmployeeNotFoundWhenEmployeeDoesNotExist() {
        // Given
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(new EmployeeQuery(EMPLOYEE_ID.value())))
                .isInstanceOf(EmployeeNotFoundException.class)
                .extracting("code").isEqualTo("EMP-404");
    }
}
