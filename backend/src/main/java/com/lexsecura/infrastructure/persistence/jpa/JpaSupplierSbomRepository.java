package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.SupplierSbomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSupplierSbomRepository extends JpaRepository<SupplierSbomEntity, UUID> {
    Optional<SupplierSbomEntity> findByIdAndOrgId(UUID id, UUID orgId);
    List<SupplierSbomEntity> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);
    void deleteByIdAndOrgId(UUID id, UUID orgId);
}
