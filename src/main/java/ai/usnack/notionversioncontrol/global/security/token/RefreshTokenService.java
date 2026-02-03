package ai.usnack.notionversioncontrol.global.security.token;

import ai.usnack.notionversioncontrol.global.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtProperties jwtProperties;

  private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

  public void saveRefreshToken(UUID userId, String tokenId, String token) {
    String key = buildKey(userId, tokenId);
    redisTemplate.opsForValue().set(key, token, jwtProperties.refreshTokenExpiry());
  }

  public Optional<String> getRefreshToken(UUID userId, String tokenId) {
    String key = buildKey(userId, tokenId);
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  public void deleteRefreshToken(UUID userId, String tokenId) {
    String key = buildKey(userId, tokenId);
    redisTemplate.delete(key);
  }

  public void deleteAllRefreshTokens(UUID userId) {
    String pattern = REFRESH_TOKEN_PREFIX + userId + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  public void scheduleTokenDeletion(UUID userId, String tokenId) {
    String key = buildKey(userId, tokenId);
    redisTemplate.expire(key, jwtProperties.refreshTokenRotationDelay());
  }

  private String buildKey(UUID userId, String tokenId) {
    return REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
  }
}
