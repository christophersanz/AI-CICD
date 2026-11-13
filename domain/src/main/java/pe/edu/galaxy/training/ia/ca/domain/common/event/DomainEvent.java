package pe.edu.galaxy.training.ia.ca.domain.common.event;

import java.time.Instant;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Hecho de negocio ocurrido en un agregado. No es {@code sealed}: sin {@code module-info} solo
 * podría permitir subtipos de este paquete. Cada agregado define su propia jerarquía sellada.
 */
public interface DomainEvent {

    /**
     * Identificador único del evento, para que un consumidor descarte duplicados.
     * @return el identificador del evento
     */
    UUID eventId();

    /**
     * Instante en que ocurrió el hecho, tomado del {@code Clock} que recibe el agregado.
     * @return el instante del hecho
     */
    Instant occurredOn();

    /**
     * Identificador del agregado que emitió el evento.
     * @return el identificador del agregado
     */
    Identifier aggregateId();
}
