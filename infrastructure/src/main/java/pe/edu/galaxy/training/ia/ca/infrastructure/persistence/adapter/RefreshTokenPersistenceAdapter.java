package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.domain.identity.model.RefreshToken;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RevocationReason;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository.RefreshTokenJpaRepository;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repository;
    private final RefreshTokenPersistenceMapper mapper;

    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repository,
                                          RefreshTokenPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        return repository.findWithLockByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return mapper.toDomain(repository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public void revokeFamily(UUID familyId, RevocationReason reason, Instant revokedAt) {
        repository.revokeFamily(familyId, reason, revokedAt);
    }
}
