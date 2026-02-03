package ai.usnack.notionversioncontrol.global.security.repository;

import ai.usnack.notionversioncontrol.global.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

  Optional<Permission> findByName(String name);

  Optional<Permission> findByResourceAndAction(String resource, String action);
}
