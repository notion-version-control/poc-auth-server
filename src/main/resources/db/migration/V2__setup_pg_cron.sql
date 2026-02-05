CREATE EXTENSION IF NOT EXISTS pg_cron;

-- Blacklist 정리: 5분 주기 (access token 30분 만료이므로 충분)
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
