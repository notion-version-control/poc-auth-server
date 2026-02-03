package ai.usnack.notionversioncontrol.global.security.entity;

import ai.usnack.notionversioncontrol.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission", uniqueConstraints = @UniqueConstraint(columnNames = { "resource", "action" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

  @Column(unique = true, nullable = false, length = 100)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(nullable = false, length = 50)
  private String resource;

  @Column(nullable = false, length = 50)
  private String action;

  public Permission(String name, String description, String resource, String action) {
    this.name = name;
    this.description = description;
    this.resource = resource;
    this.action = action;
  }

  public static Permission of(String name, String description, String resource, String action) {
    return new Permission(name, description, resource, action);
  }
}
