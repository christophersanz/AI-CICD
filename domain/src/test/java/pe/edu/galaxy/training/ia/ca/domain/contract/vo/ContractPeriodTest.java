package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException;

/**
 * Vigencia del contrato: BR-CTR-002 y 003 (creación), BR-CTR-005 (solapamiento), BR-CTR-007 (extensión),
 * BR-CTR-008 (duración) y BR-CTR-009 (fechas inclusivas).
 */
class ContractPeriodTest {

    private static final String SELF = "pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractPeriodTest#";
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 30);
    private static final ContractPeriod FIXED = ContractPeriod.fixedTerm(START, END);

    @Nested
    class Creation {

        @Test
        void shouldRejectPeriodWhenStartDateIsMissing() {
            assertThatThrownBy(() -> ContractPeriod.of(ContractType.INDEFINITE, null, null))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @Test
        void shouldRejectPeriodWhenTypeIsMissing() {
            assertThatThrownBy(() -> ContractPeriod.of(null, START, null))
                    .isInstanceOf(InvalidValueException.class)
                    .extracting("code").isEqualTo("VAL-001");
        }

        @Test
        void shouldRejectFixedTermWhenEndDateIsMissing() {
            assertThatThrownBy(() -> ContractPeriod.of(ContractType.FIXED_TERM, START, null))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @ParameterizedTest(name = "fin = inicio {0} días")
        @CsvSource({"0", "-1", "-30"})
        void shouldRejectFixedTermWhenEndDateIsNotAfterStartDate(int daysFromStart) {
            LocalDate end = START.plusDays(daysFromStart);

            assertThatThrownBy(() -> ContractPeriod.fixedTerm(START, end))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @Test
        void shouldAcceptFixedTermWhenEndDateIsTheDayAfterStartDate() {
            assertThat(ContractPeriod.fixedTerm(START, START.plusDays(1)).endDate()).contains(START.plusDays(1));
        }

        @Test
        void shouldRejectIndefiniteWhenEndDateIsPresent() {
            assertThatThrownBy(() -> ContractPeriod.of(ContractType.INDEFINITE, START, END))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @Test
        void shouldExposeDatesWhenPeriodIsValid() {
            // When
            ContractPeriod indefinite = ContractPeriod.indefinite(START);

            // Then
            assertThat(FIXED.endDate()).contains(END);
            assertThat(FIXED.isOpenEnded()).isFalse();
            assertThat(indefinite.endDate()).isEmpty();
            assertThat(indefinite.isOpenEnded()).isTrue();
        }

        @ParameterizedTest(name = "{0} compatible con {1}: {2}")
        @CsvSource({"FIXED, FIXED_TERM, true", "FIXED, INDEFINITE, false",
                    "INDEFINITE, INDEFINITE, true", "INDEFINITE, FIXED_TERM, false"})
        void shouldBeCompatibleOnlyWithTheTypeItWasBuiltFor(String kind, ContractType type, boolean expected) {
            ContractPeriod period = "FIXED".equals(kind) ? FIXED : ContractPeriod.indefinite(START);

            assertThat(period.isCompatibleWith(type)).isEqualTo(expected);
        }
    }

    @Nested
    class Contains {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({"2025-12-31, false", "2026-01-01, true", "2026-03-15, true", "2026-06-30, true",
                    "2026-07-01, false"})
        void shouldContainDatesWhenTheyAreWithinBoundariesInclusive(LocalDate date, boolean expected) {
            assertThat(FIXED.contains(date)).isEqualTo(expected);
        }

        @Test
        void shouldContainAnyLaterDateWhenPeriodIsOpenEnded() {
            assertThat(ContractPeriod.indefinite(START).contains(START.plusYears(30))).isTrue();
        }

        @Test
        void shouldNotContainDatesBeforeStartWhenPeriodIsOpenEnded() {
            assertThat(ContractPeriod.indefinite(START).contains(START.minusDays(1))).isFalse();
        }

        @Test
        void shouldRejectContainsWhenDateIsMissing() {
            assertThatThrownBy(() -> FIXED.contains(null))
                    .isInstanceOf(InvalidValueException.class)
                    .extracting("code").isEqualTo("VAL-001");
        }
    }

    /** Pares de vigencias y si se superponen (BR-CTR-005); se comprueban en ambos sentidos. */
    static Stream<Arguments> overlapCases() {
        return Stream.of(
                Arguments.of("se tocan en un día (fin = inicio del otro)", FIXED,
                        ContractPeriod.fixedTerm(END, END.plusMonths(1)), true),
                Arguments.of("uno contiene al otro", FIXED,
                        ContractPeriod.fixedTerm(START.plusMonths(1), START.plusMonths(2)), true),
                Arguments.of("contiguos sin día en común", FIXED,
                        ContractPeriod.fixedTerm(END.plusDays(1), END.plusMonths(1)), false),
                Arguments.of("indefinido que empezó antes", FIXED,
                        ContractPeriod.indefinite(START.minusYears(1)), true),
                Arguments.of("indefinido que empieza el día del fin", FIXED, ContractPeriod.indefinite(END), true),
                Arguments.of("indefinido que empieza el día siguiente al fin", FIXED,
                        ContractPeriod.indefinite(END.plusDays(1)), false),
                Arguments.of("dos indefinidos", ContractPeriod.indefinite(START), ContractPeriod.indefinite(END), true),
                Arguments.of("indefinido frente a uno que terminó el día anterior", ContractPeriod.indefinite(START),
                        ContractPeriod.fixedTerm(START.minusYears(1), START.minusDays(1)), false),
                Arguments.of("indefinido frente a uno que termina el día de su inicio", ContractPeriod.indefinite(START),
                        ContractPeriod.fixedTerm(START.minusMonths(1), START), true));
    }

    @Nested
    class Overlaps {

        @ParameterizedTest(name = "{0} → {3}")
        @MethodSource(SELF + "overlapCases")
        void shouldDetectOverlapSymmetricallyWhenPeriodsShareAtLeastOneDay(String description, ContractPeriod a,
                                                                          ContractPeriod b, boolean expected) {
            assertThat(a.overlaps(b)).as("a.overlaps(b)").isEqualTo(expected);
            assertThat(b.overlaps(a)).as("b.overlaps(a)").isEqualTo(expected);
        }

        @Test
        void shouldRejectOverlapWhenOtherPeriodIsMissing() {
            assertThatThrownBy(() -> FIXED.overlaps(null))
                    .isInstanceOf(InvalidValueException.class)
                    .extracting("code").isEqualTo("VAL-001");
        }
    }

    @Nested
    class Duration {

        @ParameterizedTest(name = "{0} a {1} → {2}")
        @CsvSource({"2026-01-01, 2026-12-31, P1Y", "2026-01-01, 2026-06-30, P6M", "2026-01-01, 2026-01-02, P2D"})
        void shouldReturnInclusiveDurationWhenPeriodHasEndDate(LocalDate start, LocalDate end, Period expected) {
            assertThat(ContractPeriod.fixedTerm(start, end).duration()).contains(expected);
        }

        @Test
        void shouldReturnEmptyDurationWhenPeriodIsOpenEnded() {
            assertThat(ContractPeriod.indefinite(START).duration()).isEmpty();
        }
    }

    static Stream<Arguments> notLaterEndDates() {
        return Stream.of(Arguments.of((Object) null), Arguments.of(END), Arguments.of(END.minusDays(1)));
    }

    @Nested
    class Extension {

        @Test
        void shouldExtendEndDateWhenNewDateIsLater() {
            assertThat(FIXED.extendTo(END.plusMonths(3))).isEqualTo(ContractPeriod.fixedTerm(START, END.plusMonths(3)));
        }

        @ParameterizedTest(name = "nueva fecha de fin {0}")
        @MethodSource(SELF + "notLaterEndDates")
        void shouldRejectExtensionWhenNewDateIsMissingOrNotLater(LocalDate newEndDate) {
            assertThatThrownBy(() -> FIXED.extendTo(newEndDate))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @Test
        void shouldRejectExtensionWhenPeriodIsOpenEnded() {
            assertThatThrownBy(() -> ContractPeriod.indefinite(START).extendTo(END))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }
    }

    @Nested
    class Equality {

        @Test
        void shouldBeEqualWithSameHashCodeWhenDatesAreEqual() {
            ContractPeriod same = ContractPeriod.fixedTerm(START, END);

            assertThat(FIXED).isEqualTo(same).hasSameHashCodeAs(same);
        }

        @Test
        void shouldBeEqualWhenOpenEndedPeriodsStartTheSameDay() {
            assertThat(ContractPeriod.indefinite(START))
                    .isEqualTo(ContractPeriod.indefinite(START))
                    .hasSameHashCodeAs(ContractPeriod.indefinite(START));
        }

        static Stream<Arguments> differentPeriods() {
            return Stream.of(
                    Arguments.of("otra fecha de fin", ContractPeriod.fixedTerm(START, END.plusDays(1))),
                    Arguments.of("otra fecha de inicio", ContractPeriod.fixedTerm(START.plusDays(1), END)),
                    Arguments.of("indefinido con el mismo inicio", ContractPeriod.indefinite(START)),
                    Arguments.of("null", null),
                    Arguments.of("otro tipo de objeto", START));
        }

        @ParameterizedTest(name = "distinto de {0}")
        @MethodSource("differentPeriods")
        void shouldNotBeEqualWhenAnyDateOrTypeDiffers(String description, Object other) {
            assertThat(FIXED.equals(other)).isFalse();
        }
    }

    @Nested
    class Representation {

        @Test
        void shouldShowIndefiniteInsteadOfEndDateWhenPeriodIsOpenEnded() {
            assertThat(ContractPeriod.indefinite(START)).hasToString("ContractPeriod[2026-01-01 - indefinite]");
        }

        @Test
        void shouldShowBothDatesWhenPeriodHasEndDate() {
            assertThat(FIXED).hasToString("ContractPeriod[2026-01-01 - 2026-06-30]");
        }
    }
}
