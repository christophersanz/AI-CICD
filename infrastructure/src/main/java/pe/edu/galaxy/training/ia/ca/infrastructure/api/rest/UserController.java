package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.CreateUserUseCase;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.CreateUserRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.UserResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.config.OpenApiConfig;

/**
 * Gestión de usuarios. En la fase 1 el permiso {@code user:manage} lo exige una regla de URL temporal
 * en {@code SecurityFilterChainConfig} (decisión del paso 0) para evitar la escalada de privilegios.
 *
 * <p>TODO(fase 2, paso 2.1 del diseño 003; BR-CTR-011): {@code @PreAuthorize("hasAuthority('user:manage')")}
 * con {@code @EnableMethodSecurity} y retirar la regla de URL temporal.
 */
@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Users", description = "Gestión de usuarios")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserApiMapper mapper;

    public UserController(CreateUserUseCase createUserUseCase, UserApiMapper mapper) {
        this.createUserUseCase = createUserUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Crear un usuario",
            description = "Aplica BR-USR-001, BR-USR-002 y BR-USR-003. Requiere el permiso user:manage.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Sin el permiso user:manage (AUTH-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Rol inexistente (AUTH-009)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Username ya registrado (AUTH-007)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422",
            description = "Contraseña débil (AUTH-008) o username/rol con formato inválido (VAL-001)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResult result = createUserUseCase.create(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }
}
