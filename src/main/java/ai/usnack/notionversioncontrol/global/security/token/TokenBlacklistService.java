package ai.usnack.notionversioncontrol.global.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String BLACKLIST_PREFIX = "auth:blacklist:";
  private static final String BLACKLISTED = "blacklisted";

  public void addToBlacklist(String tokenId, Duration ttl) {
    String key = BLACKLIST_PREFIX + tokenId;
    redisTemplate.opsForValue().set(key, BLACKLISTED, ttl);
  }

  public boolean isBlacklisted(String tokenId) {
    String key = BLACKLIST_PREFIX + tokenId;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
