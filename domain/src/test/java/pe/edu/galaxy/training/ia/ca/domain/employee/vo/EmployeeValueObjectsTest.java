package pe.edu.galaxy.training.ia.ca.domain.employee.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException;

class EmployeeValueObjectsTest {

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789", "1234567A", ""})
    void shouldRejectDniWhenItDoesNotHaveExactlyEightDigits(String value) {
        assertThatThrownBy(() -> DocumentNumber.of(DocumentType.DNI, value))
                .isInstanceOf(InvalidDocumentNumberException.class)
                .extracting("code").isEqualTo("EMP-002");
    }

    @Test
    void shouldValidateFormatWhenAskedToDocumentType() {
        assertThat(DocumentType.DNI.isValidFormat("12345678")).isTrue();
        assertThat(DocumentType.DNI.isValidFormat("1234")).isFalse();
        assertThat(DocumentType.DNI.isValidFormat(null)).isFalse();
        assertThat(DocumentType.FOREIGNER_CARD.isValidFormat("X-001")).isTrue();
    }

    @Test
    void shouldAcceptForeignerCardWhenItIsNotBlank() {
        assertThat(DocumentNumber.of(DocumentType.FOREIGNER_CARD, " 001234567 ").value())
                .isEqualTo("001234567");
    }

    @Test
    void shouldRejectForeignerCardWhenItIsBlank() {
        assertThatThrownBy(() -> DocumentNumber.of(DocumentType.FOREIGNER_CARD, "  "))
                .isInstanceOf(InvalidDocumentNumberException.class);
    }

    @Test
    void shouldRejectDocumentWhenTypeIsMissing() {
        assertThatThrownBy(() -> DocumentNumber.of(null, "12345678"))
                .isInstanceOf(InvalidDocumentNumberException.class);
    }

    @Test
    void shouldRejectFullNameWhenAnyPartIsBlank() {
        assertThatThrownBy(() -> new FullName("Ana", " ")).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> new FullName(null, "Pérez")).isInstanceOf(InvalidValueException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@", "ana@example", "ana perez@example.com", ""})
    void shouldRejectEmailWhenFormatIsInvalid(String value) {
        assertThatThrownBy(() -> new Email(value)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldCreateEmployeeIdWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();
        assertThat(EmployeeId.of(uuid.toString())).isEqualTo(EmployeeId.of(uuid));
        assertThat(EmployeeId.generate()).isNotEqualTo(EmployeeId.generate());
    }

    @Test
    void shouldRejectEmployeeIdWhenValueIsMissingOrMalformed() {
        assertThatThrownBy(() -> EmployeeId.of((UUID) null)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> EmployeeId.of((String) null)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> EmployeeId.of("123")).isInstanceOf(InvalidValueException.class);
    }
}
