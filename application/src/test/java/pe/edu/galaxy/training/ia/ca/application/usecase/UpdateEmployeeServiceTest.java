package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

@ExtendWith(MockitoExtension.class)
class UpdateEmployeeServiceTest {

    private static final EmployeeId EMPLOYEE_ID = EmployeeId.generate();

    @Mock
    private EmployeeRepositoryPort employeeRepository;

    @InjectMocks
    private UpdateEmployeeService service;

    private static Employee existingEmployee() {
        return Employee.restore(EMPLOYEE_ID, new DocumentNumber(DocumentType.DNI, "12345678"),
                new FullName("Ana", "Pérez"), new Email("ana@example.com"));
    }

    private static UpdateEmployeeCommand command(String email) {
        return new UpdateEmployeeCommand(EMPLOYEE_ID.value(), "Ana María", "Pérez Gómez", email);
    }

    @Test
    void shouldUpdateNameAndEmailKeepingDocumentWhenEmployeeExists() {
        // Given
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(existingEmployee()));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EmployeeResult result = service.update(command("ana.perez@example.com"));

        // Then
        assertThat(result.id()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(result.firstNames()).isEqualTo("Ana María");
        assertThat(result.lastNames()).isEqualTo("Pérez Gómez");
        assertThat(result.email()).isEqualTo("ana.perez@example.com");
        assertThat(result.documentType()).isEqualTo(DocumentType.DNI);
        assertThat(result.documentNumber()).isEqualTo("12345678");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void shouldThrowEmployeeNotFoundWhenEmployeeDoesNotExist() {
        // Given
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(
                new UpdateEmployeeCommand(UUID.randomUUID(), "Ana", "Pérez", "ana@example.com")))
                .isInstanceOf(EmployeeNotFoundException.class)
                .extracting("code").isEqualTo("EMP-404");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenEmailIsInvalid() {
        // Given
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(existingEmployee()));

        // When / Then
        assertThatThrownBy(() -> service.update(command("not-an-email")))
                .isInstanceOf(InvalidValueException.class);
        verify(employeeRepository, never()).save(any());
    }
}
