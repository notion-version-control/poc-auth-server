package ai.usnack.notionversioncontrol.global.security.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

  private final AuthErrorCode errorCode;

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public AuthException(AuthErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public AuthException(AuthErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
  }
}
