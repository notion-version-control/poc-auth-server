-- =============================================================================
-- V1__init.sql
-- 초기 개발 중에는 이 파일을 덮어쓰면서 관리
-- 배포 전 Flyway 활성화 시 이 파일이 초기 마이그레이션으로 사용됨
-- =============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS pg_cron;

-- =============================================================================
-- Auth & User Tables
-- =============================================================================

-- AuthAccount (soft delete 사용)
CREATE TABLE IF NOT EXISTS auth_account (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(255) UNIQUE,
    name       VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted    BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_auth_account_email ON auth_account (email);
CREATE INDEX IF NOT EXISTS idx_auth_account_deleted ON auth_account (deleted);

-- Role
CREATE TABLE IF NOT EXISTS role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_default  BOOLEAN NOT NULL DEFAULT false,
    is_system   BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID
);
CREATE INDEX IF NOT EXISTS idx_role_name ON role (name);
CREATE INDEX IF NOT EXISTS idx_role_is_default ON role (is_default);

-- Permission
CREATE TABLE IF NOT EXISTS permission (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    resource    VARCHAR(50) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT uq_permission_resource_action UNIQUE (resource, action)
);
CREATE INDEX IF NOT EXISTS idx_permission_name ON permission (name);
CREATE INDEX IF NOT EXISTS idx_permission_resource ON permission (resource);

-- AuthAccount-Role 조인 테이블
CREATE TABLE IF NOT EXISTS auth_account_roles (
    auth_account_id UUID NOT NULL REFERENCES auth_account(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (auth_account_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_auth_account_roles_auth_account_id ON auth_account_roles (auth_account_id);
CREATE INDEX IF NOT EXISTS idx_auth_account_roles_role_id ON auth_account_roles (role_id);

-- Role-Permission 조인 테이블
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions (permission_id);

-- NotionConnection (soft delete 미사용 - 엔티티에 @SoftDelete 없음)
CREATE TABLE IF NOT EXISTS notion_connection (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_account_id UUID NOT NULL REFERENCES auth_account(id) ON DELETE CASCADE,
    provider_id     VARCHAR(255) NOT NULL UNIQUE,
    workspace_id    VARCHAR(255),
    workspace_name  VARCHAR(255),
    access_token    VARCHAR(2048),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by      UUID,
    updated_by      UUID
);
CREATE INDEX IF NOT EXISTS idx_notion_connection_auth_account_id ON notion_connection (auth_account_id);
CREATE INDEX IF NOT EXISTS idx_notion_connection_provider_id ON notion_connection (provider_id);

-- =============================================================================
-- Token Tables
-- =============================================================================

-- Refresh token (일반 테이블 - 내구성 필요)
CREATE TABLE IF NOT EXISTS refresh_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    token_id   VARCHAR(255) NOT NULL UNIQUE,
    token      VARCHAR(2048) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON refresh_token (expires_at);

-- Token blacklist (UNLOGGED - 크래시 시 소실 허용, 쓰기 3배 빠름)
CREATE UNLOGGED TABLE IF NOT EXISTS blacklisted_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_id   VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_blacklisted_token_expires_at ON blacklisted_token (expires_at);

-- =============================================================================
-- pg_cron Jobs (토큰 정리 스케줄)
-- =============================================================================

-- Blacklist 정리: 5분 주기
SELECT cron.schedule(
    'cleanup-blacklisted-tokens',
    '*/5 * * * *',
    'DELETE FROM blacklisted_token WHERE expires_at <= now()'
);

-- Refresh token 정리: 1시간 주기
SELECT cron.schedule(
    'cleanup-refresh-tokens',
    '0 * * * *',
    'DELETE FROM refresh_token WHERE expires_at <= now()'
);
