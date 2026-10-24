package pe.edu.galaxy.training.ia.ca.domain.common.model;

import java.util.UUID;

/**
 * Identificador de una entidad. Es una interfaz porque los IDs son {@code record} y no pueden
 * extender clases; cada ID declara sus fábricas {@code generate()} y {@code of(...)}.
 */
public interface Identifier {
    UUID value();
}
