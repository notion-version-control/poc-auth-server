package ai.usnack.notionversioncontrol.global.security.repository;

import ai.usnack.notionversioncontrol.global.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(String name);

  List<Role> findByIsDefaultTrue();

  @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name ")
  Optional<Role> findByNameWithPermissions(String name);
}
