package pe.edu.galaxy.training.ia.ca.application.dto;

import java.time.Instant;

public record IssuedAccessToken(String value, Instant expiresAt) {

    @Override
    public String toString() {
        return "IssuedAccessToken[value=****, expiresAt=%s]".formatted(expiresAt);
    }
}
