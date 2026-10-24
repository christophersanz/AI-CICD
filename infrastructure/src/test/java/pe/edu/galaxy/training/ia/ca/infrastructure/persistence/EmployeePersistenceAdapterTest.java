package pe.edu.galaxy.training.ia.ca.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSortField;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.dto.SortDirection;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter.EmployeePersistenceAdapter;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.EmployeePersistenceMapper;

/** Se omite automáticamente si Docker no está disponible. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import({EmployeePersistenceAdapter.class, EmployeePersistenceMapper.class})
class EmployeePersistenceAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EmployeePersistenceAdapter adapter;

    private static Employee employee(DocumentType type, String number) {
        return Employee.register(new DocumentNumber(type, number), new FullName("Ana", "Pérez"),
                new Email("ana@example.com"));
    }

    @Test
    void shouldPersistEmployeeAndFindItByDocumentWhenItIsNew() {
        // Given
        Employee employee = employee(DocumentType.DNI, "12345678");

        // When
        Employee saved = adapter.save(employee);

        // Then
        assertThat(saved.id()).isEqualTo(employee.id());
        assertThat(saved.documentNumber()).isEqualTo(employee.documentNumber());
        assertThat(adapter.existsByDocumentNumber(new DocumentNumber(DocumentType.DNI, "12345678"))).isTrue();
        assertThat(adapter.existsByDocumentNumber(new DocumentNumber(DocumentType.FOREIGNER_CARD, "12345678")))
                .isFalse();
    }

    @Test
    void shouldThrowDuplicateEmployeeDocumentWhenDocumentIsSavedTwice() {
        // Given
        adapter.save(employee(DocumentType.DNI, "87654321"));

        // When / Then
        assertThatThrownBy(() -> adapter.save(employee(DocumentType.DNI, "87654321")))
                .isInstanceOf(DuplicateEmployeeDocumentException.class);
    }

    @Test
    void shouldPersistChangesWhenExistingEmployeeIsUpdated() {
        // Given
        Employee saved = adapter.save(employee(DocumentType.DNI, "11223344"));
        Employee loaded = adapter.findById(saved.id()).orElseThrow();

        // When
        loaded.update(new FullName("Ana María", "Pérez Gómez"), new Email("ana.perez@example.com"));
        adapter.save(loaded);

        // Then
        Employee reloaded = adapter.findById(saved.id()).orElseThrow();
        assertThat(reloaded.fullName()).isEqualTo(new FullName("Ana María", "Pérez Gómez"));
        assertThat(reloaded.email()).isEqualTo(new Email("ana.perez@example.com"));
        assertThat(reloaded.documentNumber()).isEqualTo(saved.documentNumber());
    }

    @Test
    void shouldReturnEmptyWhenEmployeeIdDoesNotExist() {
        assertThat(adapter.findById(EmployeeId.generate())).isEmpty();
    }

    // --- search (BR-EMP-004) ---

    private void givenStaff() {
        adapter.save(person("40000001", "José", "Pérez Gómez"));
        adapter.save(person("40000002", "Ana María", "PEREZ Ruiz"));
        adapter.save(person("40000003", "Luis", "Núñez"));
        adapter.save(person("40000004", "Carla", "Ávila"));
        adapter.save(person("40000005", "Raúl", "50%_Off"));
    }

    private static Employee person(String number, String firstNames, String lastNames) {
        return Employee.register(new DocumentNumber(DocumentType.DNI, number), new FullName(firstNames, lastNames),
                new Email("e" + number + "@example.com"));
    }

    private PageResult<Employee> search(DocumentType type, String number, String name, int page, int size,
                                        EmployeeSortField field, SortDirection direction) {
        return adapter.search(new EmployeeSearchQuery(type, number, name, page, size, field, direction));
    }

    private PageResult<Employee> searchByName(String name) {
        return search(null, null, name, 0, 20, EmployeeSortField.LAST_NAMES, SortDirection.ASC);
    }

    private static List<String> lastNames(PageResult<Employee> page) {
        return page.content().stream().map(employee -> employee.fullName().lastNames()).toList();
    }

    @Test
    void shouldFindByPartialNameIgnoringCaseAndAccentsWhenSearchingByName() {
        // Given
        givenStaff();

        // When / Then: "perez" encuentra "Pérez" y "PEREZ"; "NUNEZ" encuentra "Núñez"
        assertThat(lastNames(searchByName("perez"))).containsExactlyInAnyOrder("Pérez Gómez", "PEREZ Ruiz");
        assertThat(lastNames(searchByName("NUNEZ"))).containsExactly("Núñez");
        assertThat(lastNames(searchByName("ávila"))).containsExactly("Ávila");
    }

    @Test
    void shouldMatchAcrossFirstAndLastNamesWhenSearchTextSpansBoth() {
        // Given
        givenStaff();

        // When / Then
        assertThat(lastNames(searchByName("jose perez"))).containsExactly("Pérez Gómez");
        assertThat(lastNames(searchByName("maría"))).containsExactly("PEREZ Ruiz");
    }

    @Test
    void shouldTreatWildcardsLiterallyWhenNameContainsThem() {
        // Given
        givenStaff();

        // When / Then: "%" no debe comportarse como comodín y devolver a todos
        assertThat(lastNames(searchByName("%"))).containsExactly("50%_Off");
        assertThat(lastNames(searchByName("_"))).containsExactly("50%_Off");
    }

    @Test
    void shouldFindOnlyExactDocumentWhenSearchingByDocument() {
        // Given
        givenStaff();

        // When
        PageResult<Employee> page = search(DocumentType.DNI, "40000003", null, 0, 20,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC);

        // Then
        assertThat(lastNames(page)).containsExactly("Núñez");
        assertThat(search(DocumentType.FOREIGNER_CARD, "40000003", null, 0, 20,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC).content()).isEmpty();
        assertThat(search(DocumentType.DNI, "4000000", null, 0, 20,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC).content()).isEmpty();
    }

    @Test
    void shouldPaginateAndSortWhenListingWithoutFilters() {
        // Given
        givenStaff();

        // When
        PageResult<Employee> first = search(null, null, null, 0, 2, EmployeeSortField.DOCUMENT_NUMBER,
                SortDirection.DESC);
        PageResult<Employee> last = search(null, null, null, 2, 2, EmployeeSortField.DOCUMENT_NUMBER,
                SortDirection.DESC);

        // Then
        assertThat(first.totalElements()).isEqualTo(5);
        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(first.content()).extracting(employee -> employee.documentNumber().value())
                .containsExactly("40000005", "40000004");
        assertThat(last.content()).extracting(employee -> employee.documentNumber().value())
                .containsExactly("40000001");
    }
}
