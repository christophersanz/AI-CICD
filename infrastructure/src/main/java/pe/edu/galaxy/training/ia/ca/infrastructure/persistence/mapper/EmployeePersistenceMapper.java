package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.Email;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.FullName;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.EmployeeJpaEntity;

/**
 * Mapeo manual: el agregado solo se construye con sus fábricas ({@code restore}), que
 * MapStruct no puede invocar sin configuración que oculte las invariantes.
 */
@Component
public class EmployeePersistenceMapper {

    public EmployeeJpaEntity toEntity(Employee employee) {
        return new EmployeeJpaEntity(
                employee.id().value(),
                employee.documentNumber().type(),
                employee.documentNumber().value(),
                employee.fullName().firstNames(),
                employee.fullName().lastNames(),
                employee.email().value());
    }

    public Employee toDomain(EmployeeJpaEntity entity) {
        return Employee.restore(
                new EmployeeId(entity.getId()),
                new DocumentNumber(entity.getDocumentType(), entity.getDocumentNumber()),
                new FullName(entity.getFirstNames(), entity.getLastNames()),
                new Email(entity.getEmail()));
    }
}
