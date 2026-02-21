package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ReleaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaReleaseRepository extends JpaRepository<ReleaseEntity, UUID> {
    List<ReleaseEntity> findAllByProductId(UUID productId);
}
