package ai.usnack.notionversioncontrol.global.security.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(AuthException.class)
  public ProblemDetail handleAuthException(AuthException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        e.getErrorCode().getHttpStatus(),
        e.getMessage()
    );
    problemDetail.setProperty("errorCode", e.getErrorCode().getCode());
    return problemDetail;
  }
}
