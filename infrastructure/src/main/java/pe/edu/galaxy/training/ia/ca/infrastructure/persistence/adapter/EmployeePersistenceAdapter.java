package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter;

import java.util.Map;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSortField;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.dto.SortDirection;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.employee.model.Employee;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.EmployeeJpaEntity;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.EmployeePersistenceMapper;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository.EmployeeJpaRepository;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository.EmployeeSpecifications;

@Component
public class EmployeePersistenceAdapter implements EmployeeRepositoryPort {

    static final String UNIQUE_DOCUMENT_CONSTRAINT = "uk_employees_document";

    private static final Map<EmployeeSortField, String> SORT_PROPERTIES = Map.of(
            EmployeeSortField.LAST_NAMES, "lastNames",
            EmployeeSortField.FIRST_NAMES, "firstNames",
            EmployeeSortField.DOCUMENT_NUMBER, "documentNumber",
            EmployeeSortField.EMAIL, "email");

    private final EmployeeJpaRepository repository;
    private final EmployeePersistenceMapper mapper;

    public EmployeePersistenceAdapter(EmployeeJpaRepository repository, EmployeePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByDocumentNumber(DocumentNumber documentNumber) {
        return repository.existsByDocumentTypeAndDocumentNumber(documentNumber.type(), documentNumber.value());
    }

    @Override
    public Optional<Employee> findById(EmployeeId employeeId) {
        return repository.findById(employeeId.value()).map(mapper::toDomain);
    }

    @Override
    public PageResult<Employee> search(EmployeeSearchQuery query) {
        Sort.Direction direction = query.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        // El id desempata para que el orden sea estable entre páginas
        Sort sort = Sort.by(direction, SORT_PROPERTIES.get(query.sortField())).and(Sort.by("id"));
        Page<EmployeeJpaEntity> page = repository.findAll(EmployeeSpecifications.matching(query),
                PageRequest.of(query.page(), query.size(), sort));
        return new PageResult<>(page.map(mapper::toDomain).getContent(), query.page(), query.size(),
                page.getTotalElements());
    }

    @Override
    public Employee save(Employee employee) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(employee)));
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateDocument(ex)) {
                throw new DuplicateEmployeeDocumentException(employee.documentNumber());
            }
            throw ex;
        }
    }

    private static boolean isDuplicateDocument(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException violation
                && UNIQUE_DOCUMENT_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
    }
}
