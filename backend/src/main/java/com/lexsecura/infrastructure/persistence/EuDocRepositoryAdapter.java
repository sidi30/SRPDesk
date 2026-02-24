package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.EuDeclarationOfConformity;
import com.lexsecura.domain.repository.EuDocRepository;
import com.lexsecura.infrastructure.persistence.entity.EuDocEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EuDocRepositoryAdapter implements EuDocRepository {

    private final JpaEuDocRepository jpa;

    public EuDocRepositoryAdapter(JpaEuDocRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public EuDeclarationOfConformity save(EuDeclarationOfConformity doc) {
        return toDomain(jpa.save(toEntity(doc)));
    }

    @Override
    public Optional<EuDeclarationOfConformity> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<EuDeclarationOfConformity> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<EuDeclarationOfConformity> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private EuDeclarationOfConformity toDomain(EuDocEntity e) {
        EuDeclarationOfConformity m = new EuDeclarationOfConformity();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setDeclarationNumber(e.getDeclarationNumber());
        m.setManufacturerName(e.getManufacturerName());
        m.setManufacturerAddress(e.getManufacturerAddress());
        m.setAuthorizedRepName(e.getAuthorizedRepName());
        m.setAuthorizedRepAddress(e.getAuthorizedRepAddress());
        m.setProductName(e.getProductName());
        m.setProductIdentification(e.getProductIdentification());
        m.setConformityAssessmentModule(e.getConformityAssessmentModule());
        m.setNotifiedBodyName(e.getNotifiedBodyName());
        m.setNotifiedBodyNumber(e.getNotifiedBodyNumber());
        m.setNotifiedBodyCertificate(e.getNotifiedBodyCertificate());
        m.setHarmonisedStandards(e.getHarmonisedStandards());
        m.setAdditionalInfo(e.getAdditionalInfo());
        m.setDeclarationText(e.getDeclarationText());
        m.setSignedBy(e.getSignedBy());
        m.setSignedRole(e.getSignedRole());
        m.setSignedAt(e.getSignedAt());
        m.setStatus(e.getStatus());
        m.setPublishedAt(e.getPublishedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private EuDocEntity toEntity(EuDeclarationOfConformity m) {
        EuDocEntity e = new EuDocEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setDeclarationNumber(m.getDeclarationNumber());
        e.setManufacturerName(m.getManufacturerName());
        e.setManufacturerAddress(m.getManufacturerAddress());
        e.setAuthorizedRepName(m.getAuthorizedRepName());
        e.setAuthorizedRepAddress(m.getAuthorizedRepAddress());
        e.setProductName(m.getProductName());
        e.setProductIdentification(m.getProductIdentification());
        e.setConformityAssessmentModule(m.getConformityAssessmentModule());
        e.setNotifiedBodyName(m.getNotifiedBodyName());
        e.setNotifiedBodyNumber(m.getNotifiedBodyNumber());
        e.setNotifiedBodyCertificate(m.getNotifiedBodyCertificate());
        e.setHarmonisedStandards(m.getHarmonisedStandards());
        e.setAdditionalInfo(m.getAdditionalInfo());
        e.setDeclarationText(m.getDeclarationText());
        e.setSignedBy(m.getSignedBy());
        e.setSignedRole(m.getSignedRole());
        e.setSignedAt(m.getSignedAt());
        e.setStatus(m.getStatus());
        e.setPublishedAt(m.getPublishedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
