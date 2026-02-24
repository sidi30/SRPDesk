package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CvdPolicy;
import com.lexsecura.domain.repository.CvdPolicyRepository;
import com.lexsecura.infrastructure.persistence.entity.CvdPolicyEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CvdPolicyRepositoryAdapter implements CvdPolicyRepository {

    private final JpaCvdPolicyRepository jpa;

    public CvdPolicyRepositoryAdapter(JpaCvdPolicyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CvdPolicy save(CvdPolicy p) {
        return toDomain(jpa.save(toEntity(p)));
    }

    @Override
    public Optional<CvdPolicy> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CvdPolicy> findByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findByProductIdAndOrgId(productId, orgId).map(this::toDomain);
    }

    @Override
    public List<CvdPolicy> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private CvdPolicy toDomain(CvdPolicyEntity e) {
        CvdPolicy m = new CvdPolicy();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setContactEmail(e.getContactEmail());
        m.setContactUrl(e.getContactUrl());
        m.setPgpKeyUrl(e.getPgpKeyUrl());
        m.setPolicyUrl(e.getPolicyUrl());
        m.setDisclosureTimelineDays(e.getDisclosureTimelineDays());
        m.setAcceptsAnonymous(e.isAcceptsAnonymous());
        m.setBugBountyUrl(e.getBugBountyUrl());
        m.setAcceptedLanguages(e.getAcceptedLanguages());
        m.setScopeDescription(e.getScopeDescription());
        m.setStatus(e.getStatus());
        m.setPublishedAt(e.getPublishedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private CvdPolicyEntity toEntity(CvdPolicy m) {
        CvdPolicyEntity e = new CvdPolicyEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setContactEmail(m.getContactEmail());
        e.setContactUrl(m.getContactUrl());
        e.setPgpKeyUrl(m.getPgpKeyUrl());
        e.setPolicyUrl(m.getPolicyUrl());
        e.setDisclosureTimelineDays(m.getDisclosureTimelineDays());
        e.setAcceptsAnonymous(m.isAcceptsAnonymous());
        e.setBugBountyUrl(m.getBugBountyUrl());
        e.setAcceptedLanguages(m.getAcceptedLanguages());
        e.setScopeDescription(m.getScopeDescription());
        e.setStatus(m.getStatus());
        e.setPublishedAt(m.getPublishedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
