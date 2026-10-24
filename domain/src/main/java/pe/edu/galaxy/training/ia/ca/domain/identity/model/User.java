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

    /** Crea un usuario habilitado. La contraseña ya debe cumplir la {@code PasswordPolicy} y estar hasheada. */
    public static User create(Username username, PasswordHash passwordHash, Set<RoleName> roles) {
        return new User(UserId.generate(), username, passwordHash, roles, true);
    }

    public static User restore(UserId id, Username username, PasswordHash passwordHash, Set<RoleName> roles,
                               boolean enabled) {
        return new User(id, username, passwordHash, roles, enabled);
    }

    /** Reemplaza el hash por uno con el algoritmo o los parámetros vigentes; la contraseña no cambia. */
    public void upgradePasswordHash(PasswordHash newHash) {
        this.passwordHash = Guard.notNull(newHash, "passwordHash");
    }

    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }

    public Username username() {
        return username;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public Set<RoleName> roles() {
        return roles;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
