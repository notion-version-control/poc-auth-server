package ai.usnack.notionversioncontrol.global.security.config;

import ai.usnack.notionversioncontrol.global.security.exception.CustomAccessDeniedHandler;
import ai.usnack.notionversioncontrol.global.security.exception.CustomAuthenticationEntryPoint;
import ai.usnack.notionversioncontrol.global.security.jwt.JwtAuthenticationFilter;
import ai.usnack.notionversioncontrol.global.security.oauth.CustomOAuth2UserService;
import ai.usnack.notionversioncontrol.global.security.oauth.OAuth2AuthenticationFailureHandler;
import ai.usnack.notionversioncontrol.global.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
  private final SecurityProperties securityProperties;
  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {

    log.debug("Public paths: {}", securityProperties.publicPaths());

    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource))

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          // Public paths from configuration
          securityProperties.publicPaths().forEach(path ->
              auth.requestMatchers(path).permitAll());
          // Authenticated API paths
          auth.requestMatchers("/api/**").authenticated();
          // TODO Authority

          // Deny all other requests
          auth.anyRequest().denyAll();
        })
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(endpoint ->
                endpoint.userService(customOAuth2UserService))
            .successHandler(oAuth2SuccessHandler)
            .failureHandler(oAuth2FailureHandler))

        // TODO integrate with RestControllerAdvice
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            .accessDeniedHandler(new CustomAccessDeniedHandler()))

        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
