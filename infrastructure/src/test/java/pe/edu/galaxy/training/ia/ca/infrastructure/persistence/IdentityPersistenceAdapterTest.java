package pe.edu.galaxy.training.ia.ca.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter.RolePersistenceAdapter;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter.UserPersistenceAdapter;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.UserPersistenceMapper;

/** Usuarios y roles contra PostgreSQL con las migraciones reales. Se omite si Docker no está disponible. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import({UserPersistenceAdapter.class, UserPersistenceMapper.class, RolePersistenceAdapter.class})
class IdentityPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserPersistenceAdapter users;

    @Autowired
    private RolePersistenceAdapter roles;

    private static User user(String username, RoleName... roles) {
        return User.create(new Username(username), new PasswordHash("{bcrypt}$2a$10$hash"), Set.of(roles));
    }

    @Test
    void shouldPersistUserWithRolesAndFindItByIdAndUsername() {
        // Given
        User user = user("ana", RoleName.HR, RoleName.EMPLOYEE);

        // When
        users.save(user);

        // Then
        User byId = users.findById(user.id()).orElseThrow();
        assertThat(byId.username()).isEqualTo(new Username("ana"));
        assertThat(byId.passwordHash()).isEqualTo(user.passwordHash());
        assertThat(byId.roles()).containsExactlyInAnyOrder(RoleName.HR, RoleName.EMPLOYEE);
        assertThat(byId.isEnabled()).isTrue();
        assertThat(users.findByUsername(new Username("ANA"))).map(User::id).contains(user.id());
        assertThat(users.existsByUsername(new Username("ana"))).isTrue();
    }

    @Test
    void shouldThrowDuplicateUsernameWhenUsernameIsSavedTwice() {
        // Given
        users.save(user("luis", RoleName.HR));

        // When / Then
        assertThatThrownBy(() -> users.save(user("luis", RoleName.EMPLOYEE)))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void shouldPersistNewHashWhenPasswordHashIsUpgraded() {
        // Given
        User user = users.save(user("maria", RoleName.HR));

        // When
        user.upgradePasswordHash(new PasswordHash("{bcrypt}$2a$12$stronger"));
        users.save(user);

        // Then
        assertThat(users.findById(user.id()).orElseThrow().passwordHash().value()).isEqualTo("{bcrypt}$2a$12$stronger");
    }

    @Test
    void shouldDetectAdministratorOnlyWhenAUserHasAdminRole() {
        // Given
        assertThat(users.existsByRole(RoleName.ADMIN)).isFalse();

        // When
        users.save(user("root", RoleName.ADMIN));

        // Then
        assertThat(users.existsByRole(RoleName.ADMIN)).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertThat(users.findById(UserId.generate())).isEmpty();
        assertThat(users.findByUsername(new Username("nobody"))).isEmpty();
    }

    @Test
    void shouldLoadSeededRolesAndOmitUnknownOnesWhenSearchingByName() {
        // When
        var found = roles.findAllByNames(Set.of(RoleName.ADMIN, RoleName.HR, new RoleName("AUDITOR")));

        // Then
        assertThat(found).extracting(Role::name).containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.HR);
    }

    @Test
    void shouldReturnUnionOfPermissionsWhenUserHasSeveralRoles() {
        // When
        Set<Permission> permissions = roles.findPermissionsByRoles(Set.of(RoleName.HR, RoleName.EMPLOYEE));

        // Then
        assertThat(permissions).contains(
                new Permission("contract:create"), new Permission("contract:read:all"),
                new Permission("contract:read:own"), new Permission("employee:update"));
        assertThat(permissions).doesNotContain(new Permission("user:manage"), new Permission("system:monitor"));
    }

    @Test
    void shouldGrantAdministrationPermissionsOnlyToAdminRole() {
        assertThat(roles.findPermissionsByRoles(Set.of(RoleName.ADMIN)))
                .contains(new Permission("user:manage"), new Permission("system:monitor"))
                .doesNotContain(new Permission("contract:read:own"));
        assertThat(roles.findPermissionsByRoles(Set.of(RoleName.EMPLOYEE)))
                .containsExactly(new Permission("contract:read:own"));
        assertThat(roles.findPermissionsByRoles(Set.of())).isEmpty();
    }
}
