package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.dto.UpdateEmployeeCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateEmployeeDocumentException;
import pe.edu.galaxy.training.ia.ca.application.exception.EmployeeNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.RegisterEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSortField;
import pe.edu.galaxy.training.ia.ca.application.dto.PageResult;
import pe.edu.galaxy.training.ia.ca.application.dto.SortDirection;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.ListEmployeesUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.UpdateEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.employee.exception.InvalidDocumentNumberException;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentNumber;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.EmployeeId;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityFilterChainConfig;

@WebMvcTest(EmployeeController.class)
@Import({EmployeeApiMapperImpl.class, SecurityFilterChainConfig.class})
class EmployeeControllerTest {

    private static final String URL = "/api/v1/employees";
    private static final String VALID_BODY = """
            {
              "documentType": "DNI",
              "documentNumber": "12345999",
              "firstNames": "Ana",
              "lastNames": "Pérez",
              "email": "ana@example.com"
            }
            """;
    private static final String VALID_UPDATE_BODY = """
            {
              "firstNames": "Ana María",
              "lastNames": "Pérez Gómez",
              "email": "ana.perez@example.com"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterEmployeeUseCase registerEmployeeUseCase;

    @MockitoBean
    private UpdateEmployeeUseCase updateEmployeeUseCase;

    @MockitoBean
    private GetEmployeeUseCase getEmployeeUseCase;

    @MockitoBean
    private ListEmployeesUseCase listEmployeesUseCase;

    @Test
    void shouldReturnCreatedWithLocationWhenRequestIsValid() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(registerEmployeeUseCase.register(any())).thenReturn(
                new EmployeeResult(id, DocumentType.DNI, "12345678", "Ana", "Pérez", "ana@example.com"));

        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/employees/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.documentType").value("DNI"))
                .andExpect(jsonPath("$.documentNumber").value("12345678"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsWhenRequiredFieldsAreMissing() throws Exception {
        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:invalid-request"))
                .andExpect(jsonPath("$.errors.length()").value(5));
        verifyNoInteractions(registerEmployeeUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenDocumentTypeIsUnknown() throws Exception {
        // Given
        String body = VALID_BODY.replace("\"DNI\"", "\"PASSPORT\"");

        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("documentType"));
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void shouldReturnConflictWhenDocumentIsAlreadyRegistered() throws Exception {
        // Given
        when(registerEmployeeUseCase.register(any())).thenThrow(
                new DuplicateEmployeeDocumentException(new DocumentNumber(DocumentType.DNI, "12345678")));

        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:duplicate-employee-document"))
                .andExpect(jsonPath("$.title").value("Duplicate employee document"))
                .andExpect(jsonPath("$.code").value("EMP-001"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenDniIsInvalid() throws Exception {
        // Given
        when(registerEmployeeUseCase.register(any()))
                .thenThrow(new InvalidDocumentNumberException("DNI must have exactly 8 digits."));

        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("EMP-002"))
                .andExpect(jsonPath("$.detail").value("DNI must have exactly 8 digits."));
    }

    @Test
    void shouldReturnInternalErrorWithoutDetailsWhenUnexpectedExceptionOccurs() throws Exception {
        // Given
        when(registerEmployeeUseCase.register(any())).thenThrow(new IllegalStateException("db password=secret"));

        // When / Then
        mockMvc.perform(post(URL).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }

    @Test
    void shouldReturnOkWithUpdatedEmployeeWhenUpdateRequestIsValid() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(updateEmployeeUseCase.update(any())).thenReturn(new EmployeeResult(
                id, DocumentType.DNI, "12345678", "Ana María", "Pérez Gómez", "ana.perez@example.com"));

        // When / Then
        mockMvc.perform(put(URL + "/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.documentNumber").value("12345678"))
                .andExpect(jsonPath("$.firstNames").value("Ana María"))
                .andExpect(jsonPath("$.email").value("ana.perez@example.com"));
        ArgumentCaptor<UpdateEmployeeCommand> captor = ArgumentCaptor.forClass(UpdateEmployeeCommand.class);
        verify(updateEmployeeUseCase).update(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new UpdateEmployeeCommand(id, "Ana María", "Pérez Gómez", "ana.perez@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequiredFieldsAreMissing() throws Exception {
        // When / Then
        mockMvc.perform(put(URL + "/" + UUID.randomUUID()).with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.length()").value(3));
        verifyNoInteractions(updateEmployeeUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeIdIsNotUuid() throws Exception {
        // When / Then
        mockMvc.perform(put(URL + "/not-a-uuid").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(updateEmployeeUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeToUpdateDoesNotExist() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(updateEmployeeUseCase.update(any())).thenThrow(new EmployeeNotFoundException(new EmployeeId(id)));

        // When / Then
        mockMvc.perform(put(URL + "/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:employee-not-found"))
                .andExpect(jsonPath("$.code").value("EMP-404"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenUpdatedDataViolatesDomain() throws Exception {
        // Given
        when(updateEmployeeUseCase.update(any())).thenThrow(new InvalidValueException("Email format is invalid."));

        // When / Then
        mockMvc.perform(put(URL + "/" + UUID.randomUUID()).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VAL-001"));
    }

    // --- consulta ---

    @Test
    void shouldReturnEmployeeWhenItExists() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(getEmployeeUseCase.get(new EmployeeQuery(id))).thenReturn(
                new EmployeeResult(id, DocumentType.DNI, "12345678", "Ana", "Pérez", "ana@example.com"));

        // When / Then
        mockMvc.perform(get(URL + "/" + id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.lastNames").value("Pérez"));
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeToGetDoesNotExist() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(getEmployeeUseCase.get(any())).thenThrow(new EmployeeNotFoundException(new EmployeeId(id)));

        // When / Then
        mockMvc.perform(get(URL + "/" + id).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("EMP-404"));
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeIdToGetIsNotUuid() throws Exception {
        mockMvc.perform(get(URL + "/not-a-uuid").with(jwt()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(getEmployeeUseCase);
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingEmployeeWithoutToken() throws Exception {
        mockMvc.perform(get(URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(getEmployeeUseCase);
    }

    // --- listado ---

    @Test
    void shouldApplyDefaultsAndReturnPageWhenListingWithoutParameters() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(listEmployeesUseCase.list(any())).thenReturn(new PageResult<>(List.of(
                new EmployeeResult(id, DocumentType.DNI, "12345678", "Ana", "Pérez", "ana@example.com")), 0, 20, 41));

        // When / Then
        mockMvc.perform(get(URL).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3));
        verify(listEmployeesUseCase).list(new EmployeeSearchQuery(null, null, null, 0, 20,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC));
    }

    @Test
    void shouldMapNameFilterPagingAndSortWhenListingWithParameters() throws Exception {
        // Given
        when(listEmployeesUseCase.list(any())).thenReturn(new PageResult<>(List.of(), 2, 5, 0));

        // When / Then
        mockMvc.perform(get(URL).with(jwt())
                        .param("name", "pérez").param("page", "2").param("size", "5")
                        .param("sort", "documentNumber,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        verify(listEmployeesUseCase).list(new EmployeeSearchQuery(null, null, "pérez", 2, 5,
                EmployeeSortField.DOCUMENT_NUMBER, SortDirection.DESC));
    }

    @Test
    void shouldMapDocumentFilterWhenListingByDocument() throws Exception {
        // Given
        when(listEmployeesUseCase.list(any())).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        // When / Then
        mockMvc.perform(get(URL).with(jwt()).param("documentType", "DNI").param("documentNumber", "12345678"))
                .andExpect(status().isOk());
        verify(listEmployeesUseCase).list(new EmployeeSearchQuery(DocumentType.DNI, "12345678", null, 0, 20,
                EmployeeSortField.LAST_NAMES, SortDirection.ASC));
    }

    @Test
    void shouldReturnBadRequestWhenDocumentAndNameFiltersAreCombined() throws Exception {
        mockMvc.perform(get(URL).with(jwt()).param("documentType", "DNI").param("documentNumber", "12345678")
                        .param("name", "perez"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("documentFilterExclusive"));
        verifyNoInteractions(listEmployeesUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeExceedsMaximum() throws Exception {
        mockMvc.perform(get(URL).with(jwt()).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[0].field").value("size"));
        verifyNoInteractions(listEmployeesUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsNotAllowed() throws Exception {
        mockMvc.perform(get(URL).with(jwt()).param("sort", "id,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("sort"));
        verifyNoInteractions(listEmployeesUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenDocumentNumberIsSentWithoutType() throws Exception {
        mockMvc.perform(get(URL).with(jwt()).param("documentNumber", "12345678"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("documentFilterComplete"));
        verifyNoInteractions(listEmployeesUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNotNumeric() throws Exception {
        mockMvc.perform(get(URL).with(jwt()).param("page", "first"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(listEmployeesUseCase);
    }
}
