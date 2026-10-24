package pe.edu.galaxy.training.ia.ca.domain.common.event;

import java.time.Instant;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.common.model.Identifier;

/**
 * Hecho de negocio ocurrido en un agregado. No es {@code sealed}: sin {@code module-info} solo
 * podría permitir subtipos de este paquete. Cada agregado define su propia jerarquía sellada.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredOn();

    Identifier aggregateId();
}
