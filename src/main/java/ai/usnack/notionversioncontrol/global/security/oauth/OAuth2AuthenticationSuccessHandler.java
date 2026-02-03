package ai.usnack.notionversioncontrol.global.security.oauth;

import ai.usnack.notionversioncontrol.global.security.entity.AuthAccount;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtProperties;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtProvider;
import ai.usnack.notionversioncontrol.global.security.token.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final JwtProperties jwtProperties;

  @Value("${app.oauth2.redirect-uri}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    AuthAccount account = oAuth2User.getAuthAccount();

    Set<String> roles = account.getRoleNames();
    Set<String> permissions = account.getPermissionNames();

    String accessToken = jwtProvider.createAccessToken(account.getId(), roles, permissions);
    String refreshToken = jwtProvider.createRefreshToken(account.getId());

    Claims refreshClaims = jwtProvider.validateAndGetClaims(refreshToken);
    refreshTokenService.saveRefreshToken(account.getId(), refreshClaims.getId(), refreshToken);

    addRefreshTokenCookie(response, refreshToken);

    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .fragment("access_token=" + accessToken)
        .build()
        .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(jwtProperties.refreshTokenExpiry())
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
