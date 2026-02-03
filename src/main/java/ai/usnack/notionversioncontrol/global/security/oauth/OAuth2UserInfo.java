package ai.usnack.notionversioncontrol.global.security.oauth;

public sealed interface OAuth2UserInfo permits NotionUserInfo {

  String getProviderId();

  String getName();

  String getEmail();
}
