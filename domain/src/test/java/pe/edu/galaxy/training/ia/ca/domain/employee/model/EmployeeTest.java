package pe.edu.galaxy.training.ia.ca.domain.employee.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;

class EmployeeTest {

    private static final DocumentNumber DOCUMENT = DocumentNumber.of(DocumentType.DNI, " 12345678 ");
    private static final FullName NAME = new FullName(" Ana ", " Pérez ");
    private static final Email EMAIL = new Email("ana@example.com");

    @Test
    void shouldRegisterEmployeeWithGeneratedIdWhenDataIsValid() {
        // When
        Employee employee = Employee.register(DOCUMENT, NAME, EMAIL);

        // Then
        assertThat(employee.id()).isNotNull();
        assertThat(employee.documentNumber().value()).isEqualTo("12345678");
        assertThat(employee.fullName()).isEqualTo(new FullName("Ana", "Pérez"));
        assertThat(employee.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldBeEqualWhenRestoredWithSameId() {
        // Given
        EmployeeId id = EmployeeId.generate();

        // When
        Employee first = Employee.restore(id, DOCUMENT, NAME, EMAIL);
        Employee second = Employee.restore(id, DOCUMENT, NAME, new Email("other@example.com"));

        // Then
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(Employee.register(DOCUMENT, NAME, EMAIL));
    }

    @Test
    void shouldRejectEmployeeWhenRequiredDataIsMissing() {
        assertThatThrownBy(() -> Employee.register(null, NAME, EMAIL)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Employee.restore(null, DOCUMENT, NAME, EMAIL))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldReplaceNameAndEmailKeepingIdAndDocumentWhenUpdated() {
        // Given
        Employee employee = Employee.register(DOCUMENT, NAME, EMAIL);
        EmployeeId originalId = employee.id();

        // When
        employee.update(new FullName("Ana María", "Pérez Gómez"), new Email("ana.perez@example.com"));

        // Then
        assertThat(employee.id()).isEqualTo(originalId);
        assertThat(employee.documentNumber()).isEqualTo(DOCUMENT);
        assertThat(employee.fullName()).isEqualTo(new FullName("Ana María", "Pérez Gómez"));
        assertThat(employee.email()).isEqualTo(new Email("ana.perez@example.com"));
        assertThat(employee.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldRejectUpdateAndKeepDataWhenAnyValueIsMissing() {
        // Given
        Employee employee = Employee.register(DOCUMENT, NAME, EMAIL);
        FullName newName = new FullName("Luis", "Torres");

        // When / Then
        assertThatThrownBy(() -> employee.update(newName, null)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> employee.update(null, EMAIL)).isInstanceOf(InvalidValueException.class);
        assertThat(employee.fullName()).isEqualTo(NAME);
        assertThat(employee.email()).isEqualTo(EMAIL);
    }
}
