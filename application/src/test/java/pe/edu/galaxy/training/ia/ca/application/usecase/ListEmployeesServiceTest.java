package pe.edu.galaxy.training.ia.ca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSortField;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.dto.SortDirection;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

@ExtendWith(MockitoExtension.class)
class ListEmployeesServiceTest {

    @Mock
    private EmployeeRepositoryPort employeeRepository;

    @InjectMocks
    private ListEmployeesService service;

    private static EmployeeSearchQuery query(DocumentType type, String number, String name) {
        return new EmployeeSearchQuery(type, number, name, 0, 20, EmployeeSortField.LAST_NAMES, SortDirection.ASC);
    }

    private static Employee employee(String number, String lastNames) {
        return Employee.register(new DocumentNumber(DocumentType.DNI, number), new FullName("Ana", lastNames),
                new Email("ana@example.com"));
    }

    @Test
    void shouldReturnMappedPageWhenRepositoryFindsEmployees() {
        // Given
        EmployeeSearchQuery query = query(null, null, "perez");
        when(employeeRepository.search(query)).thenReturn(new PageResult<>(
                List.of(employee("12345678", "Pérez"), employee("87654321", "Pérez Gómez")), 0, 20, 2));

        // When
        PageResult<EmployeeResult> page = service.list(query);

        // Then
        assertThat(page.content()).extracting(EmployeeResult::lastNames).containsExactly("Pérez", "Pérez Gómez");
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.totalPages()).isEqualTo(1);
        verify(employeeRepository).search(query);
    }

    @Test
    void shouldReturnEmptyPageWhenNoEmployeeMatches() {
        // Given
        EmployeeSearchQuery query = query(DocumentType.DNI, "00000000", null);
        when(employeeRepository.search(query)).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        // When
        PageResult<EmployeeResult> page = service.list(query);

        // Then
        assertThat(page.content()).isEmpty();
        assertThat(page.totalPages()).isZero();
    }

    @Test
    void shouldTreatBlankFiltersAsAbsentWhenQueryIsCreated() {
        // When
        EmployeeSearchQuery query = query(null, "  ", "  ");

        // Then
        assertThat(query.hasDocumentFilter()).isFalse();
        assertThat(query.hasNameFilter()).isFalse();
    }

    @Test
    void shouldStripNameWhenQueryIsCreated() {
        assertThat(query(null, null, "  pérez  ").name()).isEqualTo("pérez");
    }

    @ParameterizedTest
    @CsvSource(value = {"DNI, NULL", "NULL, 12345678"}, nullValues = "NULL")
    void shouldRejectQueryWhenDocumentFilterIsIncomplete(DocumentType type, String number) {
        assertThatThrownBy(() -> query(type, number, null))
                .isInstanceOf(InvalidValueException.class)
                .extracting("code").isEqualTo("VAL-001");
    }

    @Test
    void shouldRejectQueryWhenDocumentAndNameAreCombined() {
        assertThatThrownBy(() -> query(DocumentType.DNI, "12345678", "perez"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("not both");
    }

    @ParameterizedTest
    @CsvSource({"-1, 20", "0, 0", "0, 101"})
    void shouldRejectQueryWhenPagingIsOutOfRange(int page, int size) {
        assertThatThrownBy(() -> new EmployeeSearchQuery(null, null, null, page, size,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldComputeTotalPagesWhenLastPageIsPartial() {
        assertThat(new PageResult<>(List.of(), 0, 20, 41).totalPages()).isEqualTo(3);
    }
}
