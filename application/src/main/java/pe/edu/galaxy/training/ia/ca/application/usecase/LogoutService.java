package pe.edu.galaxy.training.ia.ca.application.usecase;

import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;
import pe.edu.galaxy.training.ia.ca.application.port.in.LogoutUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;

/**
 * Cierra la sesión revocando la familia del refresh token presentado. Es idempotente: si el token no
 * existe o pertenece a otro usuario no hace nada, para no revelar información sobre tokens ajenos.
 */
@Transactional
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final TokenIssuerPort tokenIssuer;
    private final Clock clock;

    public LogoutService(RefreshTokenRepositoryPort refreshTokenRepository, TokenIssuerPort tokenIssuer,
                         Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Override
    public void logout(LogoutCommand command) {
        UserId userId = UserId.of(command.userId());
        refreshTokenRepository.findByTokenHashForUpdate(tokenIssuer.hashRefreshToken(command.refreshToken()))
                .filter(token -> token.userId().equals(userId))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(
                        token.familyId(), RevocationReason.LOGOUT, clock.instant()));
    }
}
