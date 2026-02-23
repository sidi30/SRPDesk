package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.SupplierSbom;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierSbomRepository {

    SupplierSbom save(SupplierSbom supplierSbom);

    Optional<SupplierSbom> findByIdAndOrgId(UUID id, UUID orgId);

    List<SupplierSbom> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);

    void deleteByIdAndOrgId(UUID id, UUID orgId);
}
