package ai.usnack.notionversioncontrol.global.security.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    CorsProperties cors,
    List<String> publicPaths
) {

  public record CorsProperties(
      List<String> allowedOrigins,
      boolean allowCredentials,
      long maxAge
  ) {

  }
}
