package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import java.util.Set;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.Guard;
import pe.edu.galaxy.training.ia.ca.domain.common.model.AggregateRoot;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

/** Cuenta con la que una persona se autentica. Raíz del agregado de identidad. */
public final class User extends AggregateRoot<UserId> {

    private final Username username;
    private PasswordHash passwordHash;
    private final Set<RoleName> roles;
    private final boolean enabled;

    private User(UserId id, Username username, PasswordHash passwordHash, Set<RoleName> roles, boolean enabled) {
        super(id);
        this.username = Guard.notNull(username, "username");
        this.passwordHash = Guard.notNull(passwordHash, "passwordHash");
        this.roles = Set.copyOf(Guard.notNull(roles, "roles"));
        this.enabled = enabled;
    }

    /**
     * Crea un usuario habilitado. La contraseña ya debe cumplir la {@code PasswordPolicy} y estar hasheada. La unicidad
     * del nombre la comprueba el caso de uso (BR-USR-001).
     *
     * @param username nombre de usuario normalizado
     * @param passwordHash hash de la contraseña
     * @param roles roles asignados; los permisos efectivos son la unión de los de cada rol
     * @return el usuario nuevo
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta algún dato ({@code
     *         VAL-001})
     */
    public static User create(Username username, PasswordHash passwordHash, Set<RoleName> roles) {
        return new User(UserId.generate(), username, passwordHash, roles, true);
    }

    /**
     * Reconstruye un usuario ya persistido. Uso exclusivo de los adaptadores de persistencia.
     * @param id identificador persistido
     * @param username nombre de usuario
     * @param passwordHash hash de la contraseña
     * @param roles roles asignados
     * @param enabled {@code false} si el usuario está deshabilitado
     * @return el usuario reconstruido
     */
    public static User restore(UserId id, Username username, PasswordHash passwordHash, Set<RoleName> roles,
                               boolean enabled) {
        return new User(id, username, passwordHash, roles, enabled);
    }

    /**
     * Reemplaza el hash por uno con el algoritmo o los parámetros vigentes; la contraseña no cambia. Lo hace el
     * adaptador de autenticación cuando detecta un hash desactualizado.
     *
     * @param newHash hash recalculado de la misma contraseña
     * @throws pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException si falta ({@code VAL-001})
     */
    public void upgradePasswordHash(PasswordHash newHash) {
        this.passwordHash = Guard.notNull(newHash, "passwordHash");
    }

    /**
     * Indica si el usuario tiene un rol.
     * @param role rol buscado
     * @return {@code true} si lo tiene
     */
    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }

    /** {@return el nombre de usuario normalizado} */
    public Username username() {
        return username;
    }

    /** {@return el hash de la contraseña} */
    public PasswordHash passwordHash() {
        return passwordHash;
    }

    /** {@return los roles asignados, en un conjunto inmutable} */
    public Set<RoleName> roles() {
        return roles;
    }

    /** {@return {@code false} si está deshabilitado; entonces no puede autenticarse (BR-USR-004)} */
    public boolean isEnabled() {
        return enabled;
    }
}
