package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AppliedStandard;
import com.lexsecura.domain.repository.AppliedStandardRepository;
import com.lexsecura.infrastructure.persistence.entity.AppliedStandardEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AppliedStandardRepositoryAdapter implements AppliedStandardRepository {

    private final JpaAppliedStandardRepository jpa;

    public AppliedStandardRepositoryAdapter(JpaAppliedStandardRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AppliedStandard save(AppliedStandard standard) {
        return toDomain(jpa.save(toEntity(standard)));
    }

    @Override
    public Optional<AppliedStandard> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<AppliedStandard> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private AppliedStandard toDomain(AppliedStandardEntity e) {
        AppliedStandard m = new AppliedStandard();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setStandardCode(e.getStandardCode());
        m.setStandardTitle(e.getStandardTitle());
        m.setVersion(e.getVersion());
        m.setComplianceStatus(e.getComplianceStatus());
        m.setNotes(e.getNotes());
        m.setEvidenceIds(e.getEvidenceIds());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private AppliedStandardEntity toEntity(AppliedStandard m) {
        AppliedStandardEntity e = new AppliedStandardEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setStandardCode(m.getStandardCode());
        e.setStandardTitle(m.getStandardTitle());
        e.setVersion(m.getVersion());
        e.setComplianceStatus(m.getComplianceStatus());
        e.setNotes(m.getNotes());
        e.setEvidenceIds(m.getEvidenceIds());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
