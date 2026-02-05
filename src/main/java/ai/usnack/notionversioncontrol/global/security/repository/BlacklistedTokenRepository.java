package ai.usnack.notionversioncontrol.global.security.repository;

import java.util.UUID;

import ai.usnack.notionversioncontrol.global.security.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

  boolean existsByTokenId(String tokenId);
}
