package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.Role;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Permission;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.RoleJpaEntity;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository.RoleJpaRepository;

@Component
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository repository;

    public RolePersistenceAdapter(RoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Role> findAllByNames(Set<RoleName> names) {
        return repository.findAllById(values(names)).stream()
                .map(RolePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Set<Permission> findPermissionsByRoles(Set<RoleName> names) {
        if (names.isEmpty()) {
            return Set.of();
        }
        return repository.findPermissionCodesByRoleNames(values(names)).stream()
                .map(Permission::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> values(Set<RoleName> names) {
        return names.stream().map(RoleName::value).collect(Collectors.toSet());
    }

    private static Role toDomain(RoleJpaEntity entity) {
        return new Role(new RoleName(entity.getName()),
                entity.getPermissions().stream().map(Permission::new).collect(Collectors.toSet()));
    }
}
