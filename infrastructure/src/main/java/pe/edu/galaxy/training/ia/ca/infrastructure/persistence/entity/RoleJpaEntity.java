package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/** De solo lectura: los roles y sus permisos se cargan con Flyway (V3). */
@Entity
@Table(name = "roles")
public class RoleJpaEntity {

    @Id
    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_name"))
    @Column(name = "permission_code", nullable = false, length = 64)
    private Set<String> permissions = new HashSet<>();

    protected RoleJpaEntity() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
