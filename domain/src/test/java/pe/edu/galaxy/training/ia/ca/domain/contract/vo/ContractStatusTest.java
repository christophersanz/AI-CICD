package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus.ACTIVE;
import static pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus.CANCELLED;
import static pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus.DRAFT;
import static pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus.EXPIRED;
import static pe.edu.galaxy.training.ia.ca.domain.contract.vo.ContractStatus.TERMINATED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ContractStatusTest {

    @ParameterizedTest
    @CsvSource({"DRAFT,ACTIVE", "DRAFT,CANCELLED", "ACTIVE,TERMINATED", "ACTIVE,EXPIRED"})
    void shouldAllowTransitionWhenItIsInTheStateDiagram(ContractStatus from, ContractStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"DRAFT,DRAFT", "DRAFT,TERMINATED", "DRAFT,EXPIRED", "ACTIVE,ACTIVE", "ACTIVE,DRAFT",
            "ACTIVE,CANCELLED", "TERMINATED,ACTIVE", "EXPIRED,ACTIVE", "CANCELLED,DRAFT"})
    void shouldRejectTransitionWhenItIsNotInTheStateDiagram(ContractStatus from, ContractStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ContractStatus.class, names = {"TERMINATED", "EXPIRED", "CANCELLED"})
    void shouldBeFinalWhenStatusHasNoOutgoingTransitions(ContractStatus status) {
        assertThat(status.isFinal()).isTrue();
    }

    @Test
    void shouldBeEditableOnlyWhenDraft() {
        assertThat(DRAFT.isEditable()).isTrue();
        assertThat(ACTIVE.isEditable()).isFalse();
        assertThat(DRAFT.isFinal()).isFalse();
        assertThat(ACTIVE.isFinal()).isFalse();
        assertThat(TERMINATED.isEditable()).isFalse();
        assertThat(EXPIRED.isEditable()).isFalse();
        assertThat(CANCELLED.isEditable()).isFalse();
    }
}
