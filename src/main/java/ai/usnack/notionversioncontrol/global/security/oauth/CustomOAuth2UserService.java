package ai.usnack.notionversioncontrol.global.security.oauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.usnack.notionversioncontrol.global.security.entity.AuthAccount;
import ai.usnack.notionversioncontrol.global.security.entity.NotionConnection;
import ai.usnack.notionversioncontrol.global.security.entity.Role;
import ai.usnack.notionversioncontrol.global.security.repository.AuthAccountRepository;
import ai.usnack.notionversioncontrol.global.security.repository.NotionConnectionRepository;
import ai.usnack.notionversioncontrol.global.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final AuthAccountRepository authAccountRepository;
  private final NotionConnectionRepository notionConnectionRepository;
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String accessToken = userRequest.getAccessToken().getTokenValue();

    Map<String, Object> attributes;
    OAuth2UserInfo userInfo;

    if ("notion".equals(registrationId)) {
      // Notion의 토큰 응답에 이미 사용자 정보가 포함되어 있음
      // user-info-uri 호출 없이 토큰 응답의 추가 파라미터에서 추출
      attributes = new HashMap<>(userRequest.getAdditionalParameters());
      userInfo = extractNotionUserInfo(attributes);
    } else {
      // 다른 OAuth2 프로바이더는 기본 방식 사용
      DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
      OAuth2User oauth2User = delegate.loadUser(userRequest);
      attributes = oauth2User.getAttributes();
      userInfo = extractUserInfo(registrationId, attributes);
    }

    AuthAccount authAccount = processOAuthUser(userInfo, accessToken);

    return new CustomOAuth2User(authAccount, attributes);
  }

  @SuppressWarnings("unchecked")
  private NotionUserInfo extractNotionUserInfo(Map<String, Object> attributes) {
    // Notion 토큰 응답 형식:
    // { "access_token": "...", "bot_id": "...", "workspace_id": "...",
    // "workspace_name": "...", "owner": { "type": "user", "user": {...} } }
    Map<String, Object> owner = (Map<String, Object>) attributes.get("owner");
    if (owner == null) {
      throw new OAuth2AuthenticationException("Invalid Notion token response: missing owner");
    }

    Map<String, Object> user = (Map<String, Object>) owner.get("user");
    if (user == null) {
      throw new OAuth2AuthenticationException(
          "Invalid Notion token response: missing user in owner");
    }

    String id = (String) user.get("id");
    String name = (String) user.get("name");
    String email = null;
    Map<String, Object> person = (Map<String, Object>) user.get("person");
    if (person != null) {
      email = (String) person.get("email");
    }
    String workspaceId = (String) attributes.get("workspace_id");
    String workspaceName = (String) attributes.get("workspace_name");

    return new NotionUserInfo(id, name, email, workspaceId, workspaceName);
  }

  @SuppressWarnings("unchecked")
  private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
    // 다른 OAuth2 프로바이더 지원을 위한 확장 포인트
    throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
  }

  private AuthAccount processOAuthUser(OAuth2UserInfo userInfo, String accessToken) {
    return notionConnectionRepository.findByProviderIdWithAuthAccount(userInfo.getProviderId())
        .map(connection -> {
          AuthAccount account = connection.getAuthAccount();
          account.updateProfile(userInfo.getName(), userInfo.getEmail());
          connection.updateAccessToken(accessToken);

          if (userInfo instanceof NotionUserInfo notionInfo) {
            connection.updateWorkspace(notionInfo.workspaceId(), notionInfo.workspaceName());
          }

          return account;
        })
        .orElseGet(() -> createNewAccountWithConnection(userInfo, accessToken));
  }

  private AuthAccount createNewAccountWithConnection(OAuth2UserInfo userInfo, String accessToken) {
    List<Role> defaultRoles = roleRepository.findByIsDefaultTrue();
    AuthAccount account = AuthAccount.create(userInfo.getName(), userInfo.getEmail(), defaultRoles);
    authAccountRepository.save(account);

    if (userInfo instanceof NotionUserInfo notionInfo) {
      NotionConnection connection = NotionConnection.create(
          account,
          notionInfo.getProviderId(),
          notionInfo.workspaceId(),
          notionInfo.workspaceName(),
          accessToken);
      notionConnectionRepository.save(connection);
    }

    log.info("New user registered via OAuth: {}", account.getEmail());
    return account;
  }
}
