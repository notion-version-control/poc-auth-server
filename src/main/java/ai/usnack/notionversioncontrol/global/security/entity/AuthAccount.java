package ai.usnack.notionversioncontrol.global.security.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "auth_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SoftDelete
public class AuthAccount extends BaseEntity {

  @Column(unique = true)
  private String email;

  @Column(nullable = false, length = 100)
  private String name;

  @Getter(AccessLevel.NONE)
  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "auth_account_roles", joinColumns = @JoinColumn(name = "auth_account_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  public void updateProfile(String name, String email) {
    if (name != null && !name.isBlank()) {
      this.name = name;
    }
    if (email != null && !email.isBlank()) {
      this.email = email;
    }
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
  }

  public Set<String> getRoleNames() {
    return roles.stream()
        .map(Role::getName)
        .collect(Collectors.toSet());
  }

  public Set<String> getPermissionNames() {
    return roles.stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(Permission::getName)
        .collect(Collectors.toSet());
  }

  public static AuthAccount create(String name, String email, Collection<Role> roles) {
    return AuthAccount.builder()
        .name(name)
        .email(email)
        .password(UUID.randomUUID().toString())
        .roles(new HashSet<>(roles))
        .build();
  }
}
