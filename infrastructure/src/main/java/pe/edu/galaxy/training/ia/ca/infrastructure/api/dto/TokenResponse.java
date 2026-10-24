package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Par de tokens emitido")
public record TokenResponse(
        @Schema(description = "JWT para la cabecera Authorization: Bearer") String accessToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(description = "Segundos hasta que expira el access token", example = "900") long expiresIn,
        @Schema(description = "Token opaco de un solo uso para /auth/refresh") String refreshToken,
        @Schema(description = "Segundos hasta que expira el refresh token", example = "604800") long refreshExpiresIn) {

    public static final String BEARER = "Bearer";

    @Override
    public String toString() {
        return "TokenResponse[accessToken=****, tokenType=%s, expiresIn=%d, refreshToken=****, refreshExpiresIn=%d]"
                .formatted(tokenType, expiresIn, refreshExpiresIn);
    }
}
