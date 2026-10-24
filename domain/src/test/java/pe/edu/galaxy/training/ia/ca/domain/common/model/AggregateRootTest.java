package pe.edu.galaxy.training.ia.ca.domain.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

class AggregateRootTest {

    private record SampleId(UUID value) implements Identifier {
    }

    private record SampleHappened(UUID eventId, Instant occurredOn, SampleId aggregateId) implements DomainEvent {
    }

    private static final class SampleAggregate extends AggregateRoot<SampleId> {

        SampleAggregate(SampleId id) {
            super(id);
        }

        void doSomething() {
            registerEvent(new SampleHappened(UUID.randomUUID(), Instant.EPOCH, id()));
        }
    }

    private static final class OtherAggregate extends AggregateRoot<SampleId> {

        OtherAggregate(SampleId id) {
            super(id);
        }
    }

    private static final SampleId ID = new SampleId(UUID.randomUUID());

    @Test
    void shouldReturnEventsAndClearThemWhenPulled() {
        // Given
        SampleAggregate aggregate = new SampleAggregate(ID);
        aggregate.doSomething();
        aggregate.doSomething();

        // When
        List<DomainEvent> events = aggregate.pullDomainEvents();

        // Then
        assertThat(events).hasSize(2).allSatisfy(event -> assertThat(event.aggregateId()).isEqualTo(ID));
        assertThat(aggregate.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldReturnImmutableCopyWhenEventsArePulled() {
        // Given
        SampleAggregate aggregate = new SampleAggregate(ID);
        aggregate.doSomething();

        // When
        List<DomainEvent> events = aggregate.pullDomainEvents();

        // Then
        assertThatThrownBy(events::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldBeEqualWhenSameTypeAndId() {
        assertThat(new SampleAggregate(ID)).isEqualTo(new SampleAggregate(ID)).hasSameHashCodeAs(new SampleAggregate(ID));
    }

    @Test
    void shouldNotBeEqualWhenIdOrTypeDiffers() {
        assertThat(new SampleAggregate(ID)).isNotEqualTo(new SampleAggregate(new SampleId(UUID.randomUUID())));
        assertThat(new SampleAggregate(ID)).isNotEqualTo(new OtherAggregate(ID));
    }

    @Test
    void shouldRejectEntityWhenIdIsMissing() {
        assertThatThrownBy(() -> new SampleAggregate(null))
                .isInstanceOf(InvalidValueException.class)
                .extracting("code").isEqualTo("VAL-001");
    }
}
