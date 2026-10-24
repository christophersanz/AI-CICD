package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Period;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException;

class ContractPeriodTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 30);

    @Test
    void shouldRejectPeriodWhenStartDateIsMissing() {
        assertThatThrownBy(() -> ContractPeriod.of(ContractType.INDEFINITE, null, null))
                .isInstanceOf(InvalidContractPeriodException.class)
                .extracting("code").isEqualTo("CTR-001");
    }

    @Test
    void shouldRejectPeriodWhenTypeIsMissing() {
        assertThatThrownBy(() -> ContractPeriod.of(null, START, null)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectFixedTermWhenEndDateIsMissing() {
        assertThatThrownBy(() -> ContractPeriod.of(ContractType.FIXED_TERM, START, null))
                .isInstanceOf(InvalidContractPeriodException.class);
    }

    @Test
    void shouldRejectFixedTermWhenEndDateIsNotAfterStartDate() {
        assertThatThrownBy(() -> ContractPeriod.fixedTerm(START, START))
                .isInstanceOf(InvalidContractPeriodException.class);
    }

    @Test
    void shouldRejectIndefiniteWhenEndDateIsPresent() {
        assertThatThrownBy(() -> ContractPeriod.of(ContractType.INDEFINITE, START, END))
                .isInstanceOf(InvalidContractPeriodException.class);
    }

    @Test
    void shouldExposeDatesWhenPeriodIsValid() {
        // When
        ContractPeriod fixed = ContractPeriod.fixedTerm(START, END);
        ContractPeriod indefinite = ContractPeriod.indefinite(START);

        // Then
        assertThat(fixed.endDate()).contains(END);
        assertThat(fixed.isOpenEnded()).isFalse();
        assertThat(indefinite.endDate()).isEmpty();
        assertThat(indefinite.isOpenEnded()).isTrue();
        assertThat(fixed.isCompatibleWith(ContractType.FIXED_TERM)).isTrue();
        assertThat(indefinite.isCompatibleWith(ContractType.FIXED_TERM)).isFalse();
        assertThat(indefinite.isCompatibleWith(ContractType.INDEFINITE)).isTrue();
    }

    @Test
    void shouldContainDatesWhenTheyAreWithinBoundariesInclusive() {
        ContractPeriod period = ContractPeriod.fixedTerm(START, END);

        assertThat(period.contains(START)).isTrue();
        assertThat(period.contains(END)).isTrue();
        assertThat(period.contains(START.minusDays(1))).isFalse();
        assertThat(period.contains(END.plusDays(1))).isFalse();
        assertThat(ContractPeriod.indefinite(START).contains(START.plusYears(30))).isTrue();
    }

    @Test
    void shouldDetectOverlapWhenPeriodsShareAtLeastOneDay() {
        ContractPeriod period = ContractPeriod.fixedTerm(START, END);

        assertThat(period.overlaps(ContractPeriod.fixedTerm(END, END.plusMonths(1)))).isTrue();
        assertThat(period.overlaps(ContractPeriod.indefinite(START.minusYears(1)))).isTrue();
        assertThat(ContractPeriod.indefinite(START).overlaps(ContractPeriod.indefinite(END))).isTrue();
    }

    @Test
    void shouldNotDetectOverlapWhenPeriodsAreDisjoint() {
        ContractPeriod period = ContractPeriod.fixedTerm(START, END);

        assertThat(period.overlaps(ContractPeriod.fixedTerm(END.plusDays(1), END.plusMonths(1)))).isFalse();
        assertThat(period.overlaps(ContractPeriod.indefinite(END.plusDays(1)))).isFalse();
    }

    @Test
    void shouldReturnInclusiveDurationWhenPeriodHasEndDate() {
        assertThat(ContractPeriod.fixedTerm(START, LocalDate.of(2026, 12, 31)).duration()).contains(Period.ofYears(1));
        assertThat(ContractPeriod.fixedTerm(START, END).duration()).contains(Period.ofMonths(6));
    }

    @Test
    void shouldReturnEmptyDurationWhenPeriodIsOpenEnded() {
        assertThat(ContractPeriod.indefinite(START).duration()).isEmpty();
    }

    @Test
    void shouldExtendEndDateWhenNewDateIsLater() {
        assertThat(ContractPeriod.fixedTerm(START, END).extendTo(END.plusMonths(3)))
                .isEqualTo(ContractPeriod.fixedTerm(START, END.plusMonths(3)));
    }

    @Test
    void shouldRejectExtensionWhenNewDateIsNotLaterOrPeriodIsOpenEnded() {
        assertThatThrownBy(() -> ContractPeriod.fixedTerm(START, END).extendTo(END))
                .isInstanceOf(InvalidContractPeriodException.class);
        assertThatThrownBy(() -> ContractPeriod.indefinite(START).extendTo(END))
                .isInstanceOf(InvalidContractPeriodException.class);
    }
}
