package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RefreshTokenId;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.RefreshTokenJpaEntity;

/** Mapeo manual: el agregado solo se reconstruye con {@code restore}. */
@Component
public class RefreshTokenPersistenceMapper {

    public RefreshTokenJpaEntity toEntity(RefreshToken token) {
        return new RefreshTokenJpaEntity(
                token.id().value(),
                token.userId().value(),
                token.familyId(),
                token.tokenHash(),
                token.issuedAt(),
                token.expiresAt(),
                token.usedAt().orElse(null),
                token.replacedBy().map(RefreshTokenId::value).orElse(null),
                token.revokedAt().orElse(null),
                token.revocationReason().orElse(null));
    }

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.restore(
                RefreshTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getFamilyId(),
                entity.getTokenHash(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getReplacedBy() == null ? null : RefreshTokenId.of(entity.getReplacedBy()),
                entity.getRevokedAt(),
                entity.getRevocationReason());
    }
}
