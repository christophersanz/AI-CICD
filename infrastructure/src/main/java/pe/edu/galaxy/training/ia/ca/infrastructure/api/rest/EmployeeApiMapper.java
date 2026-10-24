package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import java.util.Map;
import java.util.UUID;
import org.mapstruct.Mapper;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSortField;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.dto.RegisterEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.SortDirection;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeSearchRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.PageResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.UpdateEmployeeRequest;

@Mapper
public interface EmployeeApiMapper {

    int DEFAULT_PAGE_SIZE = 20;
    String DEFAULT_SORT = "lastNames,asc";
    Map<String, EmployeeSortField> SORT_FIELDS = Map.of(
            "lastNames", EmployeeSortField.LAST_NAMES,
            "firstNames", EmployeeSortField.FIRST_NAMES,
            "documentNumber", EmployeeSortField.DOCUMENT_NUMBER,
            "email", EmployeeSortField.EMAIL);

    RegisterEmployeeCommand toCommand(EmployeeRequest request);

    UpdateEmployeeCommand toCommand(UUID employeeId, UpdateEmployeeRequest request);

    EmployeeResponse toResponse(EmployeeResult result);

    default EmployeeQuery toQuery(UUID employeeId) {
        return new EmployeeQuery(employeeId);
    }

    /** El formato ya lo validó Bean Validation; aquí solo se aplican los valores por defecto. */
    default EmployeeSearchQuery toQuery(EmployeeSearchRequest request) {
        String[] sort = (request.sort() == null ? DEFAULT_SORT : request.sort()).split(",");
        SortDirection direction = sort.length > 1 && "desc".equals(sort[1]) ? SortDirection.DESC : SortDirection.ASC;
        DocumentType documentType = request.documentType() == null || request.documentType().isBlank()
                ? null
                : DocumentType.valueOf(request.documentType());
        return new EmployeeSearchQuery(documentType, request.documentNumber(), request.name(),
                request.page() == null ? 0 : request.page(),
                request.size() == null ? DEFAULT_PAGE_SIZE : request.size(),
                SORT_FIELDS.get(sort[0]), direction);
    }

    default PageResponse<EmployeeResponse> toResponse(PageResult<EmployeeResult> result) {
        return new PageResponse<>(result.content().stream().map(this::toResponse).toList(), result.page(),
                result.size(), result.totalElements(), result.totalPages());
    }
}
