package ai.usnack.notionversioncontrol.global.security.oauth;

public record NotionUserInfo(
    String id,
    String name,
    String email,
    String workspaceId,
    String workspaceName
) implements OAuth2UserInfo {

  @Override
  public String getProviderId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEmail() {
    return email;
  }
}
