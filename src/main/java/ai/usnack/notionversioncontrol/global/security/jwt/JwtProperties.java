package ai.usnack.notionversioncontrol.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    Duration accessTokenExpiry,
    Duration refreshTokenExpiry,
    Duration refreshTokenRotationDelay
) {

}
