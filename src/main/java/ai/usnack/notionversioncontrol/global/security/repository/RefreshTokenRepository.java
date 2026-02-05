package ai.usnack.notionversioncontrol.global.security.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import ai.usnack.notionversioncontrol.global.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByUserIdAndTokenId(UUID userId, String tokenId);

  void deleteByUserIdAndTokenId(UUID userId, String tokenId);

  void deleteAllByUserId(UUID userId);

  @Modifying
  @Query("UPDATE RefreshToken r SET r.expiresAt = :expiresAt "
       + "WHERE r.userId = :userId AND r.tokenId = :tokenId")
  int updateExpiresAt(UUID userId, String tokenId, Instant expiresAt);
}
