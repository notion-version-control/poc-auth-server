package ai.usnack.notionversioncontrol.global.security.repository;

import ai.usnack.notionversioncontrol.global.security.entity.NotionConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface NotionConnectionRepository extends JpaRepository<NotionConnection, UUID> {

  Optional<NotionConnection> findByProviderId(String providerId);

  Optional<NotionConnection> findByAuthAccountId(UUID authAccountId);

  @Query("SELECT n FROM NotionConnection n JOIN FETCH n.authAccount WHERE n.providerId = :providerId")
  Optional<NotionConnection> findByProviderIdWithAuthAccount(String providerId);
}
