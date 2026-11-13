package pe.edu.galaxy.training.ia.ca.domain.common.model;

import java.util.ArrayList;
import java.util.List;
import pe.edu.galaxy.training.ia.ca.domain.common.event.DomainEvent;

/**
 * Raíz de agregado: registra los eventos de dominio que emite hasta que se publican.
 *
 * @param <ID> tipo del identificador del agregado
 */
public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Crea la raíz sin eventos pendientes.
     * @param id identificador del agregado
     */
    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * Anota un evento para publicarlo después de persistir el agregado.
     * @param event evento emitido por un método de comportamiento
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Devuelve los eventos pendientes de publicar y los descarta del agregado, para que no se publiquen dos veces.
     *
     * @return una copia inmutable de los eventos, en el orden en que se emitieron
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
