package ai.usnack.notionversioncontrol.global.security.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.usnack.notionversioncontrol.global.security.dto.TokenResponse;
import ai.usnack.notionversioncontrol.global.security.entity.AuthAccount;
import ai.usnack.notionversioncontrol.global.security.exception.AuthErrorCode;
import ai.usnack.notionversioncontrol.global.security.exception.AuthException;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtProperties;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtProvider;
import ai.usnack.notionversioncontrol.global.security.repository.AuthAccountRepository;
import ai.usnack.notionversioncontrol.global.security.token.RefreshTokenService;
import ai.usnack.notionversioncontrol.global.security.token.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final TokenBlacklistService tokenBlacklistService;
  private final AuthAccountRepository authAccountRepository;
  private final JwtProperties jwtProperties;

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @CookieValue("refreshToken") String refreshToken,
      HttpServletResponse response) {

    Claims claims = jwtProvider.validateAndGetClaims(refreshToken);
    UUID userId = UUID.fromString(claims.getSubject());
    String tokenId = claims.getId();

    // Verify token exists in Redis
    refreshTokenService.getRefreshToken(userId, tokenId)
        .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

    // Fetch user with roles and permissions
    AuthAccount account = authAccountRepository.findByIdWithRolesAndPermissions(userId)
        .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_NOT_FOUND));

    // Create new tokens
    String newAccessToken = jwtProvider.createAccessToken(account.getId(), account.getRoleNames(),
        account.getPermissionNames());
    String newRefreshToken = jwtProvider.createRefreshToken(account.getId());

    // Save new refresh token
    Claims newRefreshClaims = jwtProvider.validateAndGetClaims(newRefreshToken);
    refreshTokenService.saveRefreshToken(userId, newRefreshClaims.getId(), newRefreshToken);

    // Schedule old token deletion (rotation delay)
    refreshTokenService.scheduleTokenDeletion(userId, tokenId);

    // Update cookie
    addRefreshTokenCookie(response, newRefreshToken);

    return ResponseEntity.ok(new TokenResponse(newAccessToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader,
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {

    // Blacklist access token
    String accessToken = authHeader.substring(7);
    Claims accessClaims = jwtProvider.validateAndGetClaims(accessToken);
    Duration accessTokenTtl = Duration.between(Instant.now(), accessClaims.getExpiration().toInstant());
    if (!accessTokenTtl.isNegative()) {
      tokenBlacklistService.addToBlacklist(accessClaims.getId(), accessTokenTtl);
    }

    // Handle refresh token if present
    if (refreshToken != null && !refreshToken.isBlank()) {
      try {
        Claims refreshClaims = jwtProvider.validateAndGetClaims(refreshToken);
        UUID userId = UUID.fromString(refreshClaims.getSubject());
        Duration refreshTokenTtl = Duration.between(Instant.now(), refreshClaims.getExpiration().toInstant());
        if (!refreshTokenTtl.isNegative()) {
          tokenBlacklistService.addToBlacklist(refreshClaims.getId(), refreshTokenTtl);
        }
        refreshTokenService.deleteRefreshToken(userId, refreshClaims.getId());
      } catch (Exception ignored) {
        // Refresh token might be invalid, proceed with logout anyway
      }
    }

    // Clear cookie
    clearRefreshTokenCookie(response);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout-all")
  public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String authHeader,
      HttpServletResponse response) {

    String accessToken = authHeader.substring(7);
    Claims accessClaims = jwtProvider.validateAndGetClaims(accessToken);
    UUID userId = UUID.fromString(accessClaims.getSubject());

    // Blacklist current access token
    Duration accessTokenTtl = Duration.between(Instant.now(), accessClaims.getExpiration().toInstant());
    if (!accessTokenTtl.isNegative()) {
      tokenBlacklistService.addToBlacklist(accessClaims.getId(), accessTokenTtl);
    }

    // Delete all refresh tokens for user
    refreshTokenService.deleteAllRefreshTokens(userId);

    // Clear cookie
    clearRefreshTokenCookie(response);

    return ResponseEntity.noContent().build();
  }

  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(true)
        .sameSite("Strict").path("/").maxAge(jwtProperties.refreshTokenExpiry()).build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void clearRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(true)
        .sameSite("Strict").path("/").maxAge(0).build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
