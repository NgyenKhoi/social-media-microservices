CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255), -- NULL nếu user chỉ login external
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES user_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    device_info TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    session_id UUID REFERENCES user_session(id) ON DELETE SET NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    chain_id VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_refresh_token_chain ON refresh_token(chain_id);
CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);

CREATE TABLE user_external_account (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL, -- GOOGLE, FACEBOOK...
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(100),
    
    access_token TEXT,       -- ENCRYPTED
    refresh_token TEXT,      -- ENCRYPTED
    token_expiry TIMESTAMP,
    scopes TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    UNIQUE (provider, provider_user_id),
    UNIQUE (user_id, provider)
);

CREATE TABLE revoked_token (
    jti VARCHAR(255) PRIMARY KEY,
    user_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
    chain_id VARCHAR(255) NOT NULL,
    revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_at TIMESTAMP NOT NULL,
    revocation_reason VARCHAR(50)
);

CREATE INDEX idx_revoked_chain ON revoked_token(chain_id);
CREATE INDEX idx_revoked_user ON revoked_token(user_id);

CREATE TABLE oauth2_client (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) UNIQUE NOT NULL,
    client_secret VARCHAR(255),
    client_name VARCHAR(100),
    authentication_methods TEXT,
    authorization_grant_types TEXT,
    redirect_uris TEXT,
    scopes TEXT,
    access_token_ttl INTEGER,
    refresh_token_ttl INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);