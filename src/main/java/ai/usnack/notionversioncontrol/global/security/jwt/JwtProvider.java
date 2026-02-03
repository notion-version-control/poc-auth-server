package ai.usnack.notionversioncontrol.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;
  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    this.secretKey = Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(jwtProperties.secret())
    );
  }

  public String createAccessToken(UUID authAccountId, Set<String> roles, Set<String> permissions) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(authAccountId.toString())
        .claim("roles", roles)
        .claim("permissions", permissions)
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(jwtProperties.accessTokenExpiry())))
        .signWith(secretKey)
        .compact();
  }

  public String createRefreshToken(UUID authAccountId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(authAccountId.toString())
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(jwtProperties.refreshTokenExpiry())))
        .signWith(secretKey)
        .compact();
  }

  public Claims validateAndGetClaims(String token) throws JwtException {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isTokenValid(String token) {
    try {
      validateAndGetClaims(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public UUID getSubject(Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  public String getTokenId(Claims claims) {
    return claims.getId();
  }

  public Instant getExpiration(Claims claims) {
    return claims.getExpiration().toInstant();
  }
}
