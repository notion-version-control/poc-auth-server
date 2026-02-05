package ai.usnack.notionversioncontrol.global.security.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blacklisted_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String tokenId;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }

  private BlacklistedToken(String tokenId, Instant expiresAt) {
    this.tokenId = tokenId;
    this.expiresAt = expiresAt;
  }

  public static BlacklistedToken create(String tokenId, Instant expiresAt) {
    return new BlacklistedToken(tokenId, expiresAt);
  }
}
