package ai.usnack.notionversioncontrol.global.security.token;

import ai.usnack.notionversioncontrol.global.security.entity.RefreshToken;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtProperties;
import ai.usnack.notionversioncontrol.global.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  @Transactional
  public void saveRefreshToken(UUID userId, String tokenId, String token) {
    Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenExpiry());
    refreshTokenRepository.save(RefreshToken.create(userId, tokenId, token, expiresAt));
  }

  @Transactional(readOnly = true)
  public Optional<String> getRefreshToken(UUID userId, String tokenId) {
    return refreshTokenRepository.findByUserIdAndTokenId(userId, tokenId)
        .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
        .map(RefreshToken::getToken);
  }

  @Transactional
  public void deleteRefreshToken(UUID userId, String tokenId) {
    refreshTokenRepository.deleteByUserIdAndTokenId(userId, tokenId);
  }

  @Transactional
  public void deleteAllRefreshTokens(UUID userId) {
    refreshTokenRepository.deleteAllByUserId(userId);
  }

  @Transactional
  public void scheduleTokenDeletion(UUID userId, String tokenId) {
    Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenRotationDelay());
    refreshTokenRepository.updateExpiresAt(userId, tokenId, expiresAt);
  }
}
