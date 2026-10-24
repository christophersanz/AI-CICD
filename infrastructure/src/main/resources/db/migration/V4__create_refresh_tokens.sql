-- Refresh tokens (BR-USR-006). Solo se guarda el SHA-256 del valor entregado al cliente.

CREATE TABLE refresh_tokens (
    id                UUID        PRIMARY KEY,
    user_id           UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    family_id         UUID        NOT NULL,
    token_hash        VARCHAR(64) NOT NULL,
    issued_at         TIMESTAMPTZ NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    used_at           TIMESTAMPTZ,
    replaced_by       UUID        REFERENCES refresh_tokens (id),
    revoked_at        TIMESTAMPTZ,
    revocation_reason VARCHAR(30),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_refresh_tokens_expiration CHECK (expires_at > issued_at),
    CONSTRAINT ck_refresh_tokens_revocation CHECK ((revoked_at IS NULL) = (revocation_reason IS NULL))
);

CREATE INDEX ix_refresh_tokens_family_id ON refresh_tokens (family_id);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
