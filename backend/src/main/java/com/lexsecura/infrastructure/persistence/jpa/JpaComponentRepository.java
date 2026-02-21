package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaComponentRepository extends JpaRepository<ComponentEntity, UUID> {
    Optional<ComponentEntity> findByPurl(String purl);

    @Query("SELECT c FROM ComponentEntity c JOIN ReleaseComponentEntity rc ON rc.componentId = c.id WHERE rc.releaseId = :releaseId")
    List<ComponentEntity> findAllByReleaseId(UUID releaseId);
}
