package pe.edu.galaxy.training.ia.ca.domain.contract.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractCreated;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractExpired;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractRenewed;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractSigned;
import pe.edu.galaxy.training.ia.ca.domain.contract.event.ContractTerminated;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.ContractNotEditableException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.ContractNotRenewableException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractPeriodException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidContractStatusTransitionException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.InvalidTerminationException;
import pe.edu.galaxy.training.ia.ca.domain.contract.exception.SalaryBelowMinimumWageException;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractId;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractNumber;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractPeriod;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractRenewal;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractType;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.JobTitle;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.MinimumWage;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.Salary;
import pe.edu.galaxy.training.ia.ca.domain.contract.vo.Termination;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;

class ContractTest {

    private static final MinimumWage MINIMUM_WAGE = MinimumWage.ofPen(new BigDecimal("1130.00"));
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 12, 31);
    private static final EmployeeId EMPLOYEE_ID = EmployeeId.generate();
    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private static Contract draftFixedTerm() {
        return Contract.create(ContractNumber.of(2026, 1), EMPLOYEE_ID, ContractType.FIXED_TERM,
                new JobTitle("Developer"), Salary.ofPen(new BigDecimal("3000")),
                ContractPeriod.fixedTerm(START, END), MINIMUM_WAGE, CLOCK);
    }

    private static Contract draftIndefinite() {
        return Contract.create(ContractNumber.of(2026, 2), EMPLOYEE_ID, ContractType.INDEFINITE,
                new JobTitle("Analyst"), Salary.ofPen(new BigDecimal("3000")),
                ContractPeriod.indefinite(START), MINIMUM_WAGE, CLOCK);
    }

    private static Contract activeFixedTerm() {
        Contract contract = draftFixedTerm();
        contract.sign(CLOCK);
        contract.pullDomainEvents();
        return contract;
    }

    private static Contract activeIndefinite() {
        Contract contract = draftIndefinite();
        contract.sign(CLOCK);
        contract.pullDomainEvents();
        return contract;
    }

    /** Compara los eventos ignorando {@code eventId}, que se genera aleatoriamente. */
    private static ListAssert<DomainEvent> assertEvents(Contract contract) {
        return assertThat(contract.pullDomainEvents()).usingRecursiveFieldByFieldElementComparatorIgnoringFields("eventId");
    }

    @Nested
    class Create {

        @Test
        void shouldCreateDraftContractAndEmitEventWhenDataIsValid() {
            // When
            Contract contract = draftFixedTerm();

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.DRAFT);
            assertThat(contract.renewals()).isEmpty();
            assertThat(contract.termination()).isEmpty();
            assertEvents(contract)
                    .containsExactly(new ContractCreated(null, NOW, contract.id(), EMPLOYEE_ID, ContractType.FIXED_TERM));
        }

        @Test
        void shouldIdentifyEventWithUniqueIdAndAggregateIdWhenEmitted() {
            // Given
            Contract contract = draftFixedTerm();
            contract.sign(CLOCK);

            // When
            List<DomainEvent> events = contract.pullDomainEvents();

            // Then
            assertThat(events).extracting(DomainEvent::eventId).doesNotContainNull().doesNotHaveDuplicates();
            assertThat(events).extracting(DomainEvent::aggregateId).containsOnly(contract.id());
        }

        @Test
        void shouldAcceptSalaryWhenItEqualsMinimumWage() {
            // When
            Contract contract = Contract.create(ContractNumber.of(2026, 3), EMPLOYEE_ID, ContractType.INDEFINITE,
                    new JobTitle("Assistant"), Salary.ofPen(new BigDecimal("1130")),
                    ContractPeriod.indefinite(START), MINIMUM_WAGE, CLOCK);

            // Then
            assertThat(contract.salary().amount().amount()).isEqualByComparingTo("1130.00");
        }

        @Test
        void shouldRejectCreationWhenSalaryIsBelowMinimumWage() {
            assertThatThrownBy(() -> Contract.create(ContractNumber.of(2026, 3), EMPLOYEE_ID,
                    ContractType.INDEFINITE, new JobTitle("Assistant"), Salary.ofPen(new BigDecimal("1129.99")),
                    ContractPeriod.indefinite(START), MINIMUM_WAGE, CLOCK))
                    .isInstanceOf(SalaryBelowMinimumWageException.class)
                    .extracting("code").isEqualTo("CTR-002");
        }

        @Test
        void shouldRejectCreationWhenPeriodDoesNotMatchType() {
            assertThatThrownBy(() -> Contract.create(ContractNumber.of(2026, 3), EMPLOYEE_ID,
                    ContractType.INDEFINITE, new JobTitle("Assistant"), Salary.ofPen(new BigDecimal("2000")),
                    ContractPeriod.fixedTerm(START, END), MINIMUM_WAGE, CLOCK))
                    .isInstanceOf(InvalidContractPeriodException.class)
                    .extracting("code").isEqualTo("CTR-001");
        }

        @Test
        void shouldRejectCreationWhenRequiredDataIsMissing() {
            assertThatThrownBy(() -> Contract.create(null, EMPLOYEE_ID, ContractType.INDEFINITE,
                    new JobTitle("Assistant"), Salary.ofPen(new BigDecimal("2000")),
                    ContractPeriod.indefinite(START), MINIMUM_WAGE, CLOCK))
                    .isInstanceOf(InvalidValueException.class);
            assertThatThrownBy(() -> Contract.create(ContractNumber.of(2026, 3), EMPLOYEE_ID,
                    ContractType.INDEFINITE, new JobTitle("Assistant"), Salary.ofPen(new BigDecimal("2000")),
                    ContractPeriod.indefinite(START), MINIMUM_WAGE, null))
                    .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateDataWhenContractIsDraft() {
            // Given
            Contract contract = draftFixedTerm();

            // When
            contract.update(ContractType.INDEFINITE, new JobTitle("Senior Developer"),
                    Salary.ofPen(new BigDecimal("5000")), ContractPeriod.indefinite(START), MINIMUM_WAGE);

            // Then
            assertThat(contract.type()).isEqualTo(ContractType.INDEFINITE);
            assertThat(contract.jobTitle().value()).isEqualTo("Senior Developer");
            assertThat(contract.period().endDate()).isEmpty();
        }

        @Test
        void shouldRejectUpdateWhenContractIsNotDraft() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.update(ContractType.FIXED_TERM, new JobTitle("Developer"),
                    Salary.ofPen(new BigDecimal("4000")), ContractPeriod.fixedTerm(START, END), MINIMUM_WAGE))
                    .isInstanceOf(ContractNotEditableException.class)
                    .hasMessageContaining("ACTIVE")
                    .extracting("code").isEqualTo("CTR-004");
        }

        @Test
        void shouldRejectUpdateWhenSalaryIsBelowMinimumWage() {
            // Given
            Contract contract = draftFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.update(ContractType.FIXED_TERM, new JobTitle("Developer"),
                    Salary.ofPen(new BigDecimal("1000")), ContractPeriod.fixedTerm(START, END), MINIMUM_WAGE))
                    .isInstanceOf(SalaryBelowMinimumWageException.class);
        }
    }

    @Nested
    class SignAndCancel {

        @Test
        void shouldActivateContractAndEmitEventWhenSigningDraft() {
            // Given
            Contract contract = draftFixedTerm();
            contract.pullDomainEvents();

            // When
            contract.sign(CLOCK);

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.ACTIVE);
            assertEvents(contract).containsExactly(new ContractSigned(null, NOW, contract.id(), START));
        }

        @Test
        void shouldRejectSignWhenContractIsNotDraft() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.sign(CLOCK))
                    .isInstanceOf(InvalidContractStatusTransitionException.class)
                    .extracting("code").isEqualTo("CTR-008");
        }

        @Test
        void shouldCancelContractWithoutEventWhenItIsDraft() {
            // Given
            Contract contract = draftFixedTerm();
            contract.pullDomainEvents();

            // When
            contract.cancel();

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.CANCELLED);
            assertThat(contract.pullDomainEvents()).isEmpty();
        }

        @Test
        void shouldRejectCancelWhenContractIsActive() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(contract::cancel).isInstanceOf(InvalidContractStatusTransitionException.class);
        }

        @Test
        void shouldRejectSignWhenContractIsCancelled() {
            // Given
            Contract contract = draftFixedTerm();
            contract.cancel();

            // When / Then
            assertThatThrownBy(() -> contract.sign(CLOCK)).isInstanceOf(InvalidContractStatusTransitionException.class);
        }
    }

    @Nested
    class Renew {

        @Test
        void shouldExtendEndDateAndRecordRenewalWhenContractIsActiveFixedTerm() {
            // Given
            Contract contract = activeFixedTerm();
            LocalDate newEnd = END.plusMonths(6);

            // When
            contract.renew(newEnd, CLOCK);

            // Then
            assertThat(contract.period().endDate()).contains(newEnd);
            assertThat(contract.renewals()).containsExactly(new ContractRenewal(END, newEnd));
            assertEvents(contract).containsExactly(new ContractRenewed(null, NOW, contract.id(), END, newEnd));
        }

        @Test
        void shouldRejectRenewalWhenNewEndDateIsNotAfterCurrentEndDate() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.renew(END, CLOCK))
                    .isInstanceOf(ContractNotRenewableException.class)
                    .extracting("code").isEqualTo("CTR-005");
            assertThatThrownBy(() -> contract.renew(null, CLOCK)).isInstanceOf(ContractNotRenewableException.class);
        }

        @Test
        void shouldRejectRenewalWhenContractIsIndefinite() {
            // Given
            Contract contract = activeIndefinite();

            // When / Then
            assertThatThrownBy(() -> contract.renew(END, CLOCK)).isInstanceOf(ContractNotRenewableException.class);
        }

        @Test
        void shouldRejectRenewalWhenContractIsDraft() {
            // Given
            Contract contract = draftFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.renew(END.plusMonths(1), CLOCK))
                    .isInstanceOf(ContractNotRenewableException.class);
        }
    }

    @Nested
    class Terminate {

        @Test
        void shouldTerminateContractAndEmitEventWhenDateIsWithinPeriod() {
            // Given
            Contract contract = activeFixedTerm();
            LocalDate date = LocalDate.of(2026, 6, 30);

            // When
            contract.terminate(date, "  Mutual agreement ", CLOCK);

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.TERMINATED);
            assertThat(contract.termination()).contains(new Termination(date, "Mutual agreement"));
            assertEvents(contract)
                    .containsExactly(new ContractTerminated(null, NOW, contract.id(), date, "Mutual agreement"));
        }

        @Test
        void shouldAcceptTerminationWhenDateIsOnPeriodBoundaries() {
            // Given
            Contract contract = activeFixedTerm();

            // When
            contract.terminate(END, "End of project", CLOCK);

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.TERMINATED);
        }

        @Test
        void shouldRejectTerminationWhenDateIsOutsidePeriod() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.terminate(START.minusDays(1), "Reason", CLOCK))
                    .isInstanceOf(InvalidTerminationException.class)
                    .extracting("code").isEqualTo("CTR-007");
        }

        @Test
        void shouldRejectTerminationWhenReasonIsBlank() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.terminate(START, " ", CLOCK))
                    .isInstanceOf(InvalidTerminationException.class);
        }

        @Test
        void shouldAcceptAnyLaterDateWhenTerminatingIndefiniteContract() {
            // Given
            Contract contract = activeIndefinite();

            // When
            contract.terminate(START.plusYears(10), "Resignation", CLOCK);

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.TERMINATED);
        }

        @Test
        void shouldRejectTerminationWhenContractIsDraft() {
            // Given
            Contract contract = draftFixedTerm();

            // When / Then
            assertThatThrownBy(() -> contract.terminate(START, "Reason", CLOCK))
                    .isInstanceOf(InvalidContractStatusTransitionException.class);
        }
    }

    @Nested
    class Expire {

        @Test
        void shouldExpireContractAndEmitEventWhenCutoffIsAfterEndDate() {
            // Given
            Contract contract = activeFixedTerm();

            // When
            contract.expire(END.plusDays(1), CLOCK);

            // Then
            assertThat(contract.status()).isEqualTo(ContractStatus.EXPIRED);
            assertEvents(contract).containsExactly(new ContractExpired(null, NOW, contract.id(), END));
        }

        @Test
        void shouldNotBeExpirableWhenCutoffIsOnEndDate() {
            // Given
            Contract contract = activeFixedTerm();

            // When / Then
            assertThat(contract.isExpirableAt(END)).isFalse();
            assertThatThrownBy(() -> contract.expire(END, CLOCK))
                    .isInstanceOf(InvalidContractStatusTransitionException.class)
                    .extracting("code").isEqualTo("CTR-008");
        }

        @Test
        void shouldNotBeExpirableWhenContractIsIndefinite() {
            // Given
            Contract contract = activeIndefinite();

            // When / Then
            assertThat(contract.isExpirableAt(END.plusYears(5))).isFalse();
            assertThatThrownBy(() -> contract.expire(END.plusYears(5), CLOCK))
                    .isInstanceOf(InvalidContractStatusTransitionException.class);
        }

        @Test
        void shouldNotBeExpirableWhenContractIsNotActive() {
            // Given
            Contract contract = draftFixedTerm();

            // When / Then
            assertThat(contract.isExpirableAt(END.plusDays(1))).isFalse();
        }
    }

    @Test
    void shouldClearEventsWhenTheyArePulled() {
        // Given
        Contract contract = draftFixedTerm();
        contract.pullDomainEvents();

        // When / Then
        assertThat(contract.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldNotEmitEventsWhenContractIsRestored() {
        // When
        Contract contract = Contract.restore(ContractId.generate(), ContractNumber.of(2026, 9), EMPLOYEE_ID,
                ContractType.FIXED_TERM, new JobTitle("Developer"), Salary.ofPen(new BigDecimal("3000")),
                ContractPeriod.fixedTerm(START, END), ContractStatus.ACTIVE, List.of(), null);

        // Then
        assertThat(contract.pullDomainEvents()).isEmpty();
        assertThat(contract.status()).isEqualTo(ContractStatus.ACTIVE);
    }
}
