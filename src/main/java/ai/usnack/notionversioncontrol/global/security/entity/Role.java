package ai.usnack.notionversioncontrol.global.security.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ai.usnack.notionversioncontrol.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

  @Column(unique = true, nullable = false, length = 50)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault = false;

  @Column(name = "is_system", nullable = false)
  private boolean isSystem = false;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  public Role(String name, String description, boolean isDefault, boolean isSystem) {
    this.name = name;
    this.description = description;
    this.isDefault = isDefault;
    this.isSystem = isSystem;
  }

  public static Role of(String name, String description, boolean isDefault, boolean isSystem) {
    return new Role(name, description, isDefault, isSystem);
  }

  public void addPermission(Permission permission) {
    this.permissions.add(permission);
  }

  public void removePermission(Permission permission) {
    this.permissions.remove(permission);
  }

  public Set<String> getPermissionNames() {
    return permissions.stream()
        .map(Permission::getName)
        .collect(Collectors.toSet());
  }
}
