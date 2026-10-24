package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;

@Entity
@Table(name = "employees")
public class EmployeeJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "first_names", nullable = false, length = 150)
    private String firstNames;

    @Column(name = "last_names", nullable = false, length = 150)
    private String lastNames;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    protected EmployeeJpaEntity() {
    }

    public EmployeeJpaEntity(UUID id, DocumentType documentType, String documentNumber, String firstNames,
                             String lastNames, String email) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getLastNames() {
        return lastNames;
    }

    public String getEmail() {
        return email;
    }
}
