package pe.edu.galaxy.training.ia.ca.domain.common.model;

import java.util.ArrayList;
import java.util.List;
import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;

/** Raíz de agregado: registra los eventos de dominio que emite hasta que se publican. */
public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /** Devuelve los eventos pendientes de publicar y los descarta del agregado. */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
