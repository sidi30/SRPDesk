package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByOrgId(UUID orgId);
    Optional<ProductEntity> findByIdAndOrgId(UUID id, UUID orgId);
    Optional<ProductEntity> findByNameAndOrgId(String name, UUID orgId);
    void deleteByIdAndOrgId(UUID id, UUID orgId);
}
