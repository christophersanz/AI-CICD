package pe.edu.galaxy.training.ia.ca.domain.identity.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;

class UserTest {

    private static final PasswordHash HASH = new PasswordHash("{bcrypt}hash");

    @Test
    void shouldCreateEnabledUserWithRolesWhenDataIsValid() {
        // When
        User user = User.create(new Username("ana"), HASH, Set.of(RoleName.HR));

        // Then
        assertThat(user.id()).isNotNull();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.hasRole(RoleName.HR)).isTrue();
        assertThat(user.hasRole(RoleName.ADMIN)).isFalse();
    }

    @Test
    void shouldNotExposeMutableRolesWhenCallerChangesOriginalSet() {
        // Given
        Set<RoleName> roles = new HashSet<>(Set.of(RoleName.HR));
        User user = User.create(new Username("ana"), HASH, roles);

        // When
        roles.add(RoleName.ADMIN);

        // Then
        assertThat(user.roles()).containsExactly(RoleName.HR);
        assertThatThrownBy(() -> user.roles().add(RoleName.ADMIN)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReplaceHashWhenPasswordHashIsUpgraded() {
        // Given
        User user = User.create(new Username("ana"), HASH, Set.of());
        PasswordHash upgraded = new PasswordHash("{bcrypt}stronger");

        // When
        user.upgradePasswordHash(upgraded);

        // Then
        assertThat(user.passwordHash()).isEqualTo(upgraded);
    }

    @Test
    void shouldRejectUserWhenRequiredDataIsMissing() {
        assertThatThrownBy(() -> User.create(null, HASH, Set.of())).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> User.create(new Username("ana"), null, Set.of()))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> User.create(new Username("ana"), HASH, null))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldCopyPermissionsWhenRoleIsCreated() {
        // Given
        Set<Permission> permissions = new HashSet<>(Set.of(new Permission("contract:create")));
        Role role = new Role(RoleName.HR, permissions);

        // When
        permissions.add(new Permission("user:manage"));

        // Then
        assertThat(role.permissions()).containsExactly(new Permission("contract:create"));
    }
}
