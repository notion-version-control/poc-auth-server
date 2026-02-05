package ai.usnack.notionversioncontrol.global.security.token;

import ai.usnack.notionversioncontrol.global.security.entity.BlacklistedToken;
import ai.usnack.notionversioncontrol.global.security.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final BlacklistedTokenRepository blacklistedTokenRepository;

  @Transactional
  public void addToBlacklist(String tokenId, Duration ttl) {
    Instant expiresAt = Instant.now().plus(ttl);
    blacklistedTokenRepository.save(BlacklistedToken.create(tokenId, expiresAt));
  }

  @Transactional(readOnly = true)
  public boolean isBlacklisted(String tokenId) {
    return blacklistedTokenRepository.existsByTokenId(tokenId);
  }
}
