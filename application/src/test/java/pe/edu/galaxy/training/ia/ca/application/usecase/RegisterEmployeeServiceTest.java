package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.RegisterEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

@ExtendWith(MockitoExtension.class)
class RegisterEmployeeServiceTest {

    @Mock
    private EmployeeRepositoryPort employeeRepository;

    @InjectMocks
    private RegisterEmployeeService service;

    private static RegisterEmployeeCommand validCommand() {
        return new RegisterEmployeeCommand(DocumentType.DNI, "12345678", "Ana", "Pérez", "ana@example.com");
    }

    @Test
    void shouldRegisterEmployeeWhenDocumentIsNotRegistered() {
        // Given
        when(employeeRepository.existsByDocumentNumber(new DocumentNumber(DocumentType.DNI, "12345678")))
                .thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EmployeeResult result = service.register(validCommand());

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.documentType()).isEqualTo(DocumentType.DNI);
        assertThat(result.documentNumber()).isEqualTo("12345678");
        assertThat(result.firstNames()).isEqualTo("Ana");
        assertThat(result.lastNames()).isEqualTo("Pérez");
        assertThat(result.email()).isEqualTo("ana@example.com");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void shouldThrowDuplicateEmployeeDocumentWhenDocumentAlreadyExists() {
        // Given
        when(employeeRepository.existsByDocumentNumber(any())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.register(validCommand()))
                .isInstanceOf(DuplicateEmployeeDocumentException.class)
                .extracting("code").isEqualTo("EMP-001");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldRejectRegistrationWhenDniIsInvalid() {
        // Given
        RegisterEmployeeCommand command =
                new RegisterEmployeeCommand(DocumentType.DNI, "1234", "Ana", "Pérez", "ana@example.com");

        // When / Then
        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(InvalidDocumentNumberException.class)
                .extracting("code").isEqualTo("EMP-002");
        verify(employeeRepository, never()).existsByDocumentNumber(any());
    }

    @Test
    void shouldRejectRegistrationWhenEmailIsInvalid() {
        // Given
        when(employeeRepository.existsByDocumentNumber(any())).thenReturn(false);
        RegisterEmployeeCommand command =
                new RegisterEmployeeCommand(DocumentType.DNI, "12345678", "Ana", "Pérez", "not-an-email");

        // When / Then
        assertThatThrownBy(() -> service.register(command)).isInstanceOf(InvalidValueException.class);
        verify(employeeRepository, never()).save(any());
    }
}
