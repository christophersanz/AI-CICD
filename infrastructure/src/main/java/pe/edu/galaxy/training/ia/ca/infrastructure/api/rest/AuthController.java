package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.port.in.AuthenticateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetCurrentUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.LogoutUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.RefreshTokenUseCase;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.CurrentUserResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.LoginRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.RefreshTokenRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.TokenResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.config.OpenApiConfig;

/**
 * Login, refresh, logout y usuario actual. {@code login} y {@code refresh} son públicas
 * ({@code SecurityFilterChainConfig.PUBLIC_PATHS}); {@code logout} y {@code me} requieren Bearer y
 * obtienen el usuario del claim {@code sub}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Emisión, rotación y revocación de tokens")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final AuthApiMapper mapper;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase, RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase, GetCurrentUserUseCase getCurrentUserUseCase,
                          AuthApiMapper mapper) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Emite un access token y un refresh token de una familia nueva (BR-USR-004).")
    @ApiResponse(responseCode = "200", description = "Credenciales válidas")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario deshabilitado (AUTH-002)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = mapper.toResponse(authenticateUserUseCase.authenticate(mapper.toCommand(request)));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens", description = "Rota el refresh token (un solo uso). "
            + "Reutilizar uno ya usado revoca toda su familia (BR-USR-006).")
    @ApiResponse(responseCode = "200", description = "Nuevo par de tokens")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401",
            description = "Refresh token inexistente, expirado, revocado o reutilizado (AUTH-005)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = mapper.toResponse(refreshTokenUseCase.refresh(mapper.toCommand(request)));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Cerrar sesión",
            description = "Revoca la familia del refresh token si pertenece al usuario autenticado. Idempotente.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    @ApiResponse(responseCode = "400", description = "Formato de entrada inválido",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.logout(mapper.toLogoutCommand(userId(jwt), request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Usuario actual", description = "Roles y permisos efectivos vigentes del usuario del token.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Access token ausente, inválido o expirado (AUTH-003, AUTH-004)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "El usuario del token ya no existe (AUTH-404)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(getCurrentUserUseCase.get(new CurrentUserQuery(userId(jwt))));
    }

    /** {@code sub} siempre es el UUID del usuario: lo emite {@code TokenIssuerAdapter} y la firma lo protege. */
    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
