package pe.edu.galaxy.training.ia.ca.application.dto;

import java.time.Instant;

/**
 * Par de tokens que recibe el cliente al iniciar sesión o rotar. Su {@code toString()} oculta ambos valores.
 * @param accessToken JWT firmado
 * @param accessTokenExpiresAt expiración del access token
 * @param refreshToken valor opaco del refresh token
 * @param refreshTokenExpiresAt expiración del refresh token
 */
public record TokenPairResult(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt) {

    /**
     * Junta los dos tokens recién emitidos.
     * @param accessToken access token firmado
     * @param refreshToken refresh token generado; su hash no sale de la aplicación
     * @return el par para el cliente
     */
    public static TokenPairResult of(IssuedAccessToken accessToken, IssuedRefreshToken refreshToken) {
        return new TokenPairResult(accessToken.value(), accessToken.expiresAt(),
                refreshToken.value(), refreshToken.expiresAt());
    }

    @Override
    public String toString() {
        return "TokenPairResult[accessToken=****, refreshToken=****]";
    }
}
