package pe.edu.galaxy.training.ia.ca.application.usecase;

import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.RefreshTokenUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;

/**
 * Rota el refresh token y emite un nuevo par (BR-USR-004, BR-USR-006).
 *
 * <p>{@code noRollbackFor}: cuando se detecta reutilización o un usuario deshabilitado, la revocación
 * de la familia debe confirmarse aunque la operación termine en {@link InvalidRefreshTokenException}.
 */
@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final TokenIssuerPort tokenIssuer;
    private final Clock clock;

    public RefreshTokenService(RefreshTokenRepositoryPort refreshTokenRepository, UserRepositoryPort userRepository,
                               RoleRepositoryPort roleRepository, TokenIssuerPort tokenIssuer, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Override
    public TokenPairResult refresh(RefreshTokenCommand command) {
        RefreshToken current = refreshTokenRepository
                .findByTokenHashForUpdate(tokenIssuer.hashRefreshToken(command.refreshToken()))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (current.isUsed()) {
            throw revokeFamily(current, RevocationReason.REUSE_DETECTED);
        }
        if (!current.isUsable(clock.instant())) {
            throw new InvalidRefreshTokenException();
        }
        User user = userRepository.findById(current.userId())
                .filter(User::isEnabled)
                .orElseThrow(() -> revokeFamily(current, RevocationReason.USER_DISABLED));

        IssuedRefreshToken next = tokenIssuer.generateRefreshToken();
        RefreshToken successor = current.rotate(next.hash(), next.expiresAt(), clock);
        refreshTokenRepository.save(successor);
        refreshTokenRepository.save(current);
        IssuedAccessToken accessToken =
                tokenIssuer.issueAccessToken(user, roleRepository.findPermissionsByRoles(user.roles()));
        return TokenPairResult.of(accessToken, next);
    }

    private InvalidRefreshTokenException revokeFamily(RefreshToken token, RevocationReason reason) {
        refreshTokenRepository.revokeFamily(token.familyId(), reason, clock.instant());
        return new InvalidRefreshTokenException();
    }
}
