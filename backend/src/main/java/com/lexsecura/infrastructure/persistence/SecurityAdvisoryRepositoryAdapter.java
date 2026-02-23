package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SecurityAdvisory;
import com.lexsecura.domain.repository.SecurityAdvisoryRepository;
import com.lexsecura.infrastructure.persistence.entity.SecurityAdvisoryEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaSecurityAdvisoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SecurityAdvisoryRepositoryAdapter implements SecurityAdvisoryRepository {

    private final JpaSecurityAdvisoryRepository jpa;

    public SecurityAdvisoryRepositoryAdapter(JpaSecurityAdvisoryRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SecurityAdvisory save(SecurityAdvisory a) { return toDomain(jpa.save(toEntity(a))); }

    @Override
    public Optional<SecurityAdvisory> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<SecurityAdvisory> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<SecurityAdvisory> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private SecurityAdvisory toDomain(SecurityAdvisoryEntity e) {
        SecurityAdvisory a = new SecurityAdvisory();
        a.setId(e.getId()); a.setOrgId(e.getOrgId()); a.setCraEventId(e.getCraEventId());
        a.setProductId(e.getProductId()); a.setTitle(e.getTitle()); a.setSeverity(e.getSeverity());
        a.setAffectedVersions(e.getAffectedVersions()); a.setDescription(e.getDescription());
        a.setRemediation(e.getRemediation()); a.setAdvisoryUrl(e.getAdvisoryUrl());
        a.setStatus(e.getStatus()); a.setPublishedAt(e.getPublishedAt()); a.setNotifiedAt(e.getNotifiedAt());
        a.setCreatedBy(e.getCreatedBy()); a.setCreatedAt(e.getCreatedAt()); a.setUpdatedAt(e.getUpdatedAt());
        return a;
    }

    private SecurityAdvisoryEntity toEntity(SecurityAdvisory a) {
        SecurityAdvisoryEntity e = new SecurityAdvisoryEntity();
        e.setId(a.getId()); e.setOrgId(a.getOrgId()); e.setCraEventId(a.getCraEventId());
        e.setProductId(a.getProductId()); e.setTitle(a.getTitle()); e.setSeverity(a.getSeverity());
        e.setAffectedVersions(a.getAffectedVersions()); e.setDescription(a.getDescription());
        e.setRemediation(a.getRemediation()); e.setAdvisoryUrl(a.getAdvisoryUrl());
        e.setStatus(a.getStatus()); e.setPublishedAt(a.getPublishedAt()); e.setNotifiedAt(a.getNotifiedAt());
        e.setCreatedBy(a.getCreatedBy()); e.setCreatedAt(a.getCreatedAt()); e.setUpdatedAt(a.getUpdatedAt());
        return e;
    }
}
