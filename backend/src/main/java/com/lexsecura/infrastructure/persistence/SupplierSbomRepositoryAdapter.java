package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SupplierSbom;
import com.lexsecura.domain.repository.SupplierSbomRepository;
import com.lexsecura.infrastructure.persistence.entity.SupplierSbomEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaSupplierSbomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SupplierSbomRepositoryAdapter implements SupplierSbomRepository {

    private final JpaSupplierSbomRepository jpa;

    public SupplierSbomRepositoryAdapter(JpaSupplierSbomRepository jpa) { this.jpa = jpa; }

    @Override
    public SupplierSbom save(SupplierSbom s) {
        var e = toEntity(s); e = jpa.save(e); return toDomain(e);
    }
    @Override
    public Optional<SupplierSbom> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }
    @Override
    public List<SupplierSbom> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgId(releaseId, orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }
    @Override
    public void deleteByIdAndOrgId(UUID id, UUID orgId) {
        jpa.deleteByIdAndOrgId(id, orgId);
    }

    private SupplierSbom toDomain(SupplierSbomEntity e) {
        SupplierSbom s = new SupplierSbom();
        s.setId(e.getId()); s.setOrgId(e.getOrgId()); s.setReleaseId(e.getReleaseId());
        s.setSupplierName(e.getSupplierName()); s.setSupplierUrl(e.getSupplierUrl());
        s.setEvidenceId(e.getEvidenceId()); s.setComponentCount(e.getComponentCount());
        s.setFormat(e.getFormat()); s.setImportedAt(e.getImportedAt());
        s.setImportedBy(e.getImportedBy());
        return s;
    }
    private SupplierSbomEntity toEntity(SupplierSbom s) {
        SupplierSbomEntity e = new SupplierSbomEntity();
        e.setId(s.getId()); e.setOrgId(s.getOrgId()); e.setReleaseId(s.getReleaseId());
        e.setSupplierName(s.getSupplierName()); e.setSupplierUrl(s.getSupplierUrl());
        e.setEvidenceId(s.getEvidenceId()); e.setComponentCount(s.getComponentCount());
        e.setFormat(s.getFormat()); e.setImportedAt(s.getImportedAt());
        e.setImportedBy(s.getImportedBy());
        return e;
    }
}
