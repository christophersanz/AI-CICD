package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;
import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.CurrentUserResponse;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.LoginRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.RefreshTokenRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.TokenResponse;

/** Mapeo manual: {@code expiresIn} se calcula con el {@link Clock} a partir de los instantes de expiración. */
@Component
public class AuthApiMapper {

    private final Clock clock;

    public AuthApiMapper(Clock clock) {
        this.clock = clock;
    }

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.username(), request.password());
    }

    public RefreshTokenCommand toCommand(RefreshTokenRequest request) {
        return new RefreshTokenCommand(request.refreshToken());
    }

    public LogoutCommand toLogoutCommand(UUID userId, RefreshTokenRequest request) {
        return new LogoutCommand(userId, request.refreshToken());
    }

    public TokenResponse toResponse(TokenPairResult result) {
        Instant now = clock.instant();
        return new TokenResponse(result.accessToken(), TokenResponse.BEARER,
                secondsUntil(now, result.accessTokenExpiresAt()), result.refreshToken(),
                secondsUntil(now, result.refreshTokenExpiresAt()));
    }

    public CurrentUserResponse toResponse(CurrentUserResult result) {
        return new CurrentUserResponse(result.id(), result.username(), sorted(result.roles()),
                sorted(result.permissions()));
    }

    private static long secondsUntil(Instant now, Instant expiresAt) {
        return Math.max(0, Duration.between(now, expiresAt).toSeconds());
    }

    private static List<String> sorted(Collection<String> values) {
        return values.stream().sorted().toList();
    }
}
