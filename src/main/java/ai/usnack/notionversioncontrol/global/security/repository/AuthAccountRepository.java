package ai.usnack.notionversioncontrol.global.security.repository;

import ai.usnack.notionversioncontrol.global.security.entity.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, UUID> {

  Optional<AuthAccount> findByEmail(String email);

  @Query("SELECT a FROM AuthAccount a LEFT JOIN FETCH a.roles r LEFT JOIN FETCH r.permissions WHERE a.id = :id")
  Optional<AuthAccount> findByIdWithRolesAndPermissions(UUID id);
}
