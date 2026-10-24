package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Refresh token que se rota ({@code /auth/refresh}) o cuya familia se revoca ({@code /auth/logout}). */
@Schema(description = "Refresh token emitido por login o refresh")
public record RefreshTokenRequest(
        @NotBlank @Size(max = 128) String refreshToken) {

    @Override
    public String toString() {
        return "RefreshTokenRequest[refreshToken=****]";
    }
}
