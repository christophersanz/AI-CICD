package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

class ContractNumberTest {

    @Test
    void shouldFormatNumberWhenBuiltFromYearAndSequence() {
        assertThat(ContractNumber.of(2026, 123).value()).isEqualTo("CTR-2026-000123");
    }

    @Test
    void shouldNormalizeNumberWhenParsedWithSpacesAndLowercase() {
        // When
        ContractNumber number = ContractNumber.parse("  ctr-2026-000123 ");

        // Then
        assertThat(number.value()).isEqualTo("CTR-2026-000123");
        assertThat(number.year()).isEqualTo(2026);
        assertThat(number.sequence()).isEqualTo(123);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"CTR-2026-123", "CTR-26-000123", "ABC-2026-000123", "CTR-2026-0001234"})
    void shouldRejectNumberWhenFormatIsInvalid(String value) {
        assertThatThrownBy(() -> ContractNumber.parse(value)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectSequenceWhenItIsOutOfRange() {
        assertThatThrownBy(() -> ContractNumber.of(2026, 0)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> ContractNumber.of(2026, 1_000_000)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectYearWhenItDoesNotHaveFourDigits() {
        assertThatThrownBy(() -> ContractNumber.of(999, 1)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> ContractNumber.of(10_000, 1)).isInstanceOf(InvalidValueException.class);
    }
}
