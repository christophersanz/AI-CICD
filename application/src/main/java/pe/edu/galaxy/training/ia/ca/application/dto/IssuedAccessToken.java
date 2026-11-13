package pe.edu.galaxy.training.ia.ca.application.dto;

import java.time.Instant;

/**
 * Access token recién firmado. Su {@code toString()} oculta el valor.
 * @param value JWT firmado
 * @param expiresAt instante de expiración
 */
public record IssuedAccessToken(String value, Instant expiresAt) {

    @Override
    public String toString() {
        return "IssuedAccessToken[value=****, expiresAt=%s]".formatted(expiresAt);
    }
}
