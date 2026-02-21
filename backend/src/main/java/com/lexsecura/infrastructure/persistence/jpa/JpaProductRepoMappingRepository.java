package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ProductRepoMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepoMappingRepository extends JpaRepository<ProductRepoMappingEntity, UUID> {
    Optional<ProductRepoMappingEntity> findByForgeAndProjectId(String forge, long projectId);
}
