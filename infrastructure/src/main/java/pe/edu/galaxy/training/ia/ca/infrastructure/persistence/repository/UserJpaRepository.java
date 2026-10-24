package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.UserJpaEntity;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByUsername(String username);

    Optional<UserJpaEntity> findByUsername(String username);

    @Query("""
            select case when count(u) > 0 then true else false end
              from UserJpaEntity u join u.roles r
             where r = :roleName
            """)
    boolean existsByRole(@Param("roleName") String roleName);
}
