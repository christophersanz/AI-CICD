package pe.edu.galaxy.training.ia.ca.application.dto;

import java.time.Instant;

public record TokenPairResult(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt) {

    public static TokenPairResult of(IssuedAccessToken accessToken, IssuedRefreshToken refreshToken) {
        return new TokenPairResult(accessToken.value(), accessToken.expiresAt(),
                refreshToken.value(), refreshToken.expiresAt());
    }

    @Override
    public String toString() {
        return "TokenPairResult[accessToken=****, refreshToken=****]";
    }
}
