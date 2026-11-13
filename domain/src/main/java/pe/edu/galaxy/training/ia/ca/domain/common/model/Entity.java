package pe.edu.galaxy.training.ia.ca.domain.common.model;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;

/**
 * Entidad con identidad propia: dos entidades son iguales si tienen el mismo tipo e identificador.
 *
 * @param <ID> tipo del identificador
 */
public abstract class Entity<ID extends Identifier> {

    private final ID id;

    /**
     * Fija la identidad, que no cambia nunca.
     * @param id identificador de la entidad
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    protected Entity(ID id) {
        this.id = Guard.notNull(id, "id");
    }

    /** {@return el identificador de la entidad} */
    public ID id() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || (o != null && getClass() == o.getClass() && id.equals(((Entity<?>) o).id));
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
