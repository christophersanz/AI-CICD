package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.RoleJpaEntity;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String> {

    @Query("select distinct p from RoleJpaEntity r join r.permissions p where r.name in :names")
    Set<String> findPermissionCodesByRoleNames(@Param("names") Collection<String> names);
}
