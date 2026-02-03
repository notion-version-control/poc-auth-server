package ai.usnack.notionversioncontrol.global.security.jwt;

import ai.usnack.notionversioncontrol.global.security.token.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final TokenBlacklistService tokenBlacklistService;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = resolveToken(request);

    if (StringUtils.hasText(token)) {
      try {
        Claims claims = jwtProvider.validateAndGetClaims(token);
        String tokenId = claims.getId();

        if (tokenBlacklistService.isBlacklisted(tokenId)) {
          log.debug("Blacklisted token used: {}", tokenId);
          SecurityContextHolder.clearContext();
        } else {
          Authentication authentication = createAuthentication(claims);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (JwtException e) {
        log.debug("Invalid JWT token: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/");
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Authentication createAuthentication(Claims claims) {
    UUID authAccountId = UUID.fromString(claims.getSubject());
    List<String> permissions = claims.get("permissions", List.class);

    List<SimpleGrantedAuthority> authorities = permissions.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();

    return new UsernamePasswordAuthenticationToken(authAccountId, null, authorities);
  }
}
