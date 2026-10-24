package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException;

class ContractValueObjectsTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 30);

    @Test
    void shouldExposeTypeCapabilitiesWhenTypeIsFixedTermOrIndefinite() {
        assertThat(ContractType.FIXED_TERM.requiresEndDate()).isTrue();
        assertThat(ContractType.FIXED_TERM.isRenewable()).isTrue();
        assertThat(ContractType.FIXED_TERM.isExpirable()).isTrue();
        assertThat(ContractType.INDEFINITE.requiresEndDate()).isFalse();
        assertThat(ContractType.INDEFINITE.isRenewable()).isFalse();
        assertThat(ContractType.INDEFINITE.isExpirable()).isFalse();
    }

    @Test
    void shouldCreateContractIdWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        assertThat(ContractId.of(uuid).value()).isEqualTo(uuid);
        assertThat(ContractId.of(" " + uuid + " ")).isEqualTo(ContractId.of(uuid));
        assertThat(ContractId.generate()).isNotEqualTo(ContractId.generate());
    }

    @Test
    void shouldRejectContractIdWhenValueIsMissingOrMalformed() {
        assertThatThrownBy(() -> ContractId.of((UUID) null)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> ContractId.of((String) null)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> ContractId.of("not-a-uuid")).isInstanceOf(InvalidValueException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "  ab  "})
    void shouldRejectJobTitleWhenItIsTooShort(String value) {
        assertThatThrownBy(() -> new JobTitle(value)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectJobTitleWhenItIsTooLong() {
        assertThatThrownBy(() -> new JobTitle("x".repeat(101))).isInstanceOf(InvalidValueException.class);
        assertThat(new JobTitle("x".repeat(100)).value()).hasSize(100);
    }

    @Test
    void shouldRejectRenewalWhenNewEndDateIsNotAfterPrevious() {
        assertThatThrownBy(() -> new ContractRenewal(DATE, DATE)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> new ContractRenewal(null, DATE)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldNormalizeReasonWhenTerminationIsValid() {
        assertThat(new Termination(DATE, "  Mutual agreement ").reason()).isEqualTo("Mutual agreement");
    }

    @Test
    void shouldRejectTerminationWhenDateOrReasonIsMissing() {
        assertThatThrownBy(() -> new Termination(null, "Reason"))
                .isInstanceOf(InvalidTerminationException.class)
                .extracting("code").isEqualTo("CTR-007");
        assertThatThrownBy(() -> new Termination(DATE, " ")).isInstanceOf(InvalidTerminationException.class);
    }
}
