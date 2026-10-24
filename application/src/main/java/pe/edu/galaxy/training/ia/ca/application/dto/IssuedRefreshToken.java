package pe.edu.galaxy.training.ia.ca.application.dto;

import java.time.Instant;

/**
 * Refresh token recién generado.
 *
 * @param value valor que se entrega al cliente; nunca se persiste
 * @param hash  hash del valor; es lo único que se persiste
 */
public record IssuedRefreshToken(String value, String hash, Instant expiresAt) {

    @Override
    public String toString() {
        return "IssuedRefreshToken[value=****, expiresAt=%s]".formatted(expiresAt);
    }
}
