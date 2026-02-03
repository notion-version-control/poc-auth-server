package ai.usnack.notionversioncontrol.global.security.oauth;

import ai.usnack.notionversioncontrol.global.security.entity.AuthAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

  private final AuthAccount authAccount;
  private final Map<String, Object> attributes;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomOAuth2User(AuthAccount authAccount, Map<String, Object> attributes) {
    this.authAccount = authAccount;
    this.attributes = attributes;
    this.authorities = authAccount.getPermissionNames().stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getName() {
    return authAccount.getId().toString();
  }
}
