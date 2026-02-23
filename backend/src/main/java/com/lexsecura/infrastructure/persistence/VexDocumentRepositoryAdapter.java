package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.vex.VexDocument;
import com.lexsecura.domain.model.vex.VexFormat;
import com.lexsecura.domain.repository.VexDocumentRepository;
import com.lexsecura.infrastructure.persistence.entity.VexDocumentEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaVexDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VexDocumentRepositoryAdapter implements VexDocumentRepository {

    private final JpaVexDocumentRepository jpa;

    public VexDocumentRepositoryAdapter(JpaVexDocumentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public VexDocument save(VexDocument doc) {
        return toDomain(jpa.save(toEntity(doc)));
    }

    @Override
    public Optional<VexDocument> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<VexDocument> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<VexDocument> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgIdOrderByCreatedAtDesc(releaseId, orgId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private VexDocument toDomain(VexDocumentEntity e) {
        VexDocument d = new VexDocument();
        d.setId(e.getId());
        d.setOrgId(e.getOrgId());
        d.setReleaseId(e.getReleaseId());
        d.setFormat(VexFormat.valueOf(e.getFormat()));
        d.setVersion(e.getVersion());
        d.setStatus(e.getStatus());
        d.setDocumentJson(e.getDocumentJson());
        d.setSha256Hash(e.getSha256Hash());
        d.setGeneratedBy(e.getGeneratedBy());
        d.setPublishedAt(e.getPublishedAt());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    private VexDocumentEntity toEntity(VexDocument d) {
        VexDocumentEntity e = new VexDocumentEntity();
        e.setId(d.getId());
        e.setOrgId(d.getOrgId());
        e.setReleaseId(d.getReleaseId());
        e.setFormat(d.getFormat().name());
        e.setVersion(d.getVersion());
        e.setStatus(d.getStatus());
        e.setDocumentJson(d.getDocumentJson());
        e.setSha256Hash(d.getSha256Hash());
        e.setGeneratedBy(d.getGeneratedBy());
        e.setPublishedAt(d.getPublishedAt());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
