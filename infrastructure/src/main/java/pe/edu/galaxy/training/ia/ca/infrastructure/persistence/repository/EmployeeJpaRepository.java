package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.EmployeeJpaEntity;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeJpaEntity, UUID>,
        JpaSpecificationExecutor<EmployeeJpaEntity> {

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
