-- Usuarios, roles y permisos (ADR-0005). La relación rol → permisos se carga en V3.

CREATE TABLE roles (
    name        VARCHAR(30)  PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

CREATE TABLE permissions (
    code        VARCHAR(64)  PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

CREATE TABLE role_permissions (
    role_name       VARCHAR(30) NOT NULL REFERENCES roles (name),
    permission_code VARCHAR(64) NOT NULL REFERENCES permissions (code),
    PRIMARY KEY (role_name, permission_code)
);

CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    -- BR-USR-001/002: el dominio normaliza a minúsculas; la restricción evita duplicados por mayúsculas
    CONSTRAINT ck_users_username_lowercase CHECK (username = LOWER(username))
);

CREATE TABLE user_roles (
    user_id   UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_name VARCHAR(30) NOT NULL REFERENCES roles (name),
    PRIMARY KEY (user_id, role_name)
);

CREATE INDEX ix_user_roles_role_name ON user_roles (role_name);
