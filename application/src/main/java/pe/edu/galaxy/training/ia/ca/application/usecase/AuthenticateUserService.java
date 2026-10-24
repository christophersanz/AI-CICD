package pe.edu.galaxy.training.ia.ca.application.usecase;

import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedAccessToken;
import pe.edu.galaxy.training.ia.ca.application.dto.IssuedRefreshToken;
import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException;
import pe.edu.galaxy.training.ia.ca.application.port.in.AuthenticateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.CredentialsAuthenticatorPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.User;

/** Inicia sesión con usuario y contraseña y emite un par de tokens en una familia nueva (BR-USR-004). */
@Transactional
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final CredentialsAuthenticatorPort credentialsAuthenticator;
    private final RoleRepositoryPort roleRepository;
    private final TokenIssuerPort tokenIssuer;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final Clock clock;

    public AuthenticateUserService(CredentialsAuthenticatorPort credentialsAuthenticator,
                                   RoleRepositoryPort roleRepository, TokenIssuerPort tokenIssuer,
                                   RefreshTokenRepositoryPort refreshTokenRepository, Clock clock) {
        this.credentialsAuthenticator = credentialsAuthenticator;
        this.roleRepository = roleRepository;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Override
    public TokenPairResult authenticate(LoginCommand command) {
        User user = credentialsAuthenticator.authenticate(command.username(), command.password());
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }
        IssuedAccessToken accessToken =
                tokenIssuer.issueAccessToken(user, roleRepository.findPermissionsByRoles(user.roles()));
        IssuedRefreshToken refreshToken = tokenIssuer.generateRefreshToken();
        refreshTokenRepository.save(
                RefreshToken.issue(user.id(), refreshToken.hash(), refreshToken.expiresAt(), clock));
        return TokenPairResult.of(accessToken, refreshToken);
    }
}
