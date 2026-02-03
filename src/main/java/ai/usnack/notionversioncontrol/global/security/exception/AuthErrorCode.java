package ai.usnack.notionversioncontrol.global.security.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

  // Authentication errors (AUTH_xxx)
  INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
  EXPIRED_TOKEN("AUTH_002", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED),
  TOKEN_BLACKLISTED("AUTH_003", "블랙리스트에 등록된 토큰입니다", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_NOT_FOUND("AUTH_004", "Refresh Token이 존재하지 않습니다", HttpStatus.UNAUTHORIZED),
  OAUTH_FAILED("AUTH_005", "OAuth 인증에 실패했습니다", HttpStatus.UNAUTHORIZED),

  // Authorization errors (AUTHZ_xxx)
  UNAUTHENTICATED("AUTHZ_001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED("AUTHZ_002", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
  OWNERSHIP_DENIED("AUTHZ_003", "리소스에 대한 소유권이 없습니다", HttpStatus.FORBIDDEN),

  // Account errors (ACCOUNT_xxx)
  ACCOUNT_NOT_FOUND("ACCOUNT_001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
