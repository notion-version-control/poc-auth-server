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
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, unique = true)
  private String tokenId;

  @Column(nullable = false, length = 2048)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }

  private RefreshToken(UUID userId, String tokenId, String token, Instant expiresAt) {
    this.userId = userId;
    this.tokenId = tokenId;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public static RefreshToken create(UUID userId, String tokenId, String token, Instant expiresAt) {
    return new RefreshToken(userId, tokenId, token, expiresAt);
  }
}
