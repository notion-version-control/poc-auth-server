/-- Refresh token (일반 테이블 - 내구성 필요)
CREATE TABLE refresh_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    token_id   VARCHAR(255) NOT NULL,
    token      VARCHAR(2048) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_refresh_token_token_id UNIQUE (token_id)
);
CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);

-- Token blacklist (UNLOGGED - 크래시 시 소실 허용, 쓰기 3배 빠름)
CREATE UNLOGGED TABLE blacklisted_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_id   VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_blacklisted_token_token_id UNIQUE (token_id)
);
CREATE INDEX idx_blacklisted_token_expires_at ON blacklisted_token (expires_at);
