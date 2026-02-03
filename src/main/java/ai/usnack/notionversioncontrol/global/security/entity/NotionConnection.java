package ai.usnack.notionversioncontrol.global.security.entity;

import ai.usnack.notionversioncontrol.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "notion_connection", uniqueConstraints = @UniqueConstraint(columnNames = {
    "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionConnection extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auth_account_id", nullable = false)
  private AuthAccount authAccount;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

  @Column(name = "workspace_id")
  private String workspaceId;

  @Column(name = "workspace_name")
  private String workspaceName;

  @Column(name = "access_token")
  private String accessToken;

  private NotionConnection(AuthAccount authAccount, String providerId,
      String workspaceId, String workspaceName, String accessToken) {
    this.authAccount = authAccount;
    this.providerId = providerId;
    this.workspaceId = workspaceId;
    this.workspaceName = workspaceName;
    this.accessToken = accessToken;
  }

  public static NotionConnection create(AuthAccount authAccount, String providerId,
      String workspaceId, String workspaceName, String accessToken) {
    return new NotionConnection(authAccount, providerId, workspaceId, workspaceName, accessToken);
  }

  public void updateAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void updateWorkspace(String workspaceId, String workspaceName) {
    this.workspaceId = workspaceId;
    this.workspaceName = workspaceName;
  }
}
