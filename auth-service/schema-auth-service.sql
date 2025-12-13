
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES app_user(id),
    role_id BIGINT NOT NULL REFERENCES user_role(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_external_account (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) UNIQUE NOT NULL,
    provider_email VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (user_id, provider)
);

CREATE TABLE revoked_token (
    jti VARCHAR(255) PRIMARY KEY,
    revoked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    chain_id VARCHAR(255) NOT NULL,
    revocation_reason VARCHAR(50) 
);