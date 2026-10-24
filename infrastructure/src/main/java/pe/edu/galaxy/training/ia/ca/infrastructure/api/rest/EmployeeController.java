package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.ListEmployeesUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.RegisterEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.UpdateEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.EmployeeSearchRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.PageResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.UpdateEmployeeRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.config.OpenApiConfig;

/**
 * Registro, actualización, consulta y listado de empleados. Cada operación exige su permiso
 * ({@code employee:create}, {@code employee:update}, {@code employee:read}; business-rules §4.1).
 */
@RestController
@RequestMapping("/api/v1/employees")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Employees", description = "Registro, actualización, consulta y listado de empleados")
public class EmployeeController {

    private final RegisterEmployeeUseCase registerEmployeeUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final EmployeeApiMapper mapper;

    public EmployeeController(RegisterEmployeeUseCase registerEmployeeUseCase,
                              UpdateEmployeeUseCase updateEmployeeUseCase, GetEmployeeUseCase getEmployeeUseCase,
                              ListEmployeesUseCase listEmployeesUseCase, EmployeeApiMapper mapper) {
        this.registerEmployeeUseCase = registerEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un empleado")
    @ApiResponse(responseCode = "200", description = "Empleado encontrado")
    @ApiResponse(responseCode = "400", description = "Identificador con formato inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado (EMP-404)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Sin el permiso employee:read (AUTH-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAuthority('employee:read')")
    public EmployeeResponse get(@PathVariable UUID id) {
        return mapper.toResponse(getEmployeeUseCase.get(mapper.toQuery(id)));
    }

    @GetMapping
    @Operation(summary = "Listar empleados",
            description = "Paginado. Filtros opcionales: documento exacto (tipo + número) y nombre parcial "
                    + "sin distinguir mayúsculas ni tildes (BR-EMP-004).")
    @ApiResponse(responseCode = "200", description = "Página de empleados")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Sin el permiso employee:read (AUTH-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAuthority('employee:read')")
    public PageResponse<EmployeeResponse> list(@Valid @ParameterObject EmployeeSearchRequest request) {
        return mapper.toResponse(listEmployeesUseCase.list(mapper.toQuery(request)));
    }

    @PostMapping
    @Operation(summary = "Registrar un empleado", description = "Aplica BR-EMP-001 y BR-EMP-002.")
    @ApiResponse(responseCode = "201", description = "Empleado registrado")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Documento ya registrado (EMP-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Documento o datos inválidos (EMP-002, VAL-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Sin el permiso employee:create (AUTH-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAuthority('employee:create')")
    public ResponseEntity<EmployeeResponse> register(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResult result = registerEmployeeUseCase.register(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un empleado",
            description = "Reemplaza nombres y correo. El documento de identidad no se modifica (BR-EMP-003).")
    @ApiResponse(responseCode = "200", description = "Empleado actualizado")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado (EMP-404)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Datos inválidos (VAL-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Sin el permiso employee:update (AUTH-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResult result = updateEmployeeUseCase.update(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
