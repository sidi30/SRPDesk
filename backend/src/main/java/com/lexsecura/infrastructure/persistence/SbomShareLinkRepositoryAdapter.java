package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SbomShareLink;
import com.lexsecura.domain.repository.SbomShareLinkRepository;
import com.lexsecura.infrastructure.persistence.entity.SbomShareLinkEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaSbomShareLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SbomShareLinkRepositoryAdapter implements SbomShareLinkRepository {

    private final JpaSbomShareLinkRepository jpa;

    public SbomShareLinkRepositoryAdapter(JpaSbomShareLinkRepository jpa) { this.jpa = jpa; }

    @Override
    public SbomShareLink save(SbomShareLink s) {
        var e = toEntity(s); e = jpa.save(e); return toDomain(e);
    }
    @Override
    public Optional<SbomShareLink> findByToken(String token) {
        return jpa.findByToken(token).map(this::toDomain);
    }
    @Override
    public Optional<SbomShareLink> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }
    @Override
    public List<SbomShareLink> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgId(releaseId, orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private SbomShareLink toDomain(SbomShareLinkEntity e) {
        SbomShareLink s = new SbomShareLink();
        s.setId(e.getId()); s.setOrgId(e.getOrgId()); s.setReleaseId(e.getReleaseId());
        s.setEvidenceId(e.getEvidenceId()); s.setToken(e.getToken());
        s.setRecipientEmail(e.getRecipientEmail()); s.setRecipientOrg(e.getRecipientOrg());
        s.setExpiresAt(e.getExpiresAt()); s.setMaxDownloads(e.getMaxDownloads());
        s.setDownloadCount(e.getDownloadCount()); s.setIncludeVex(e.isIncludeVex());
        s.setIncludeQualityScore(e.isIncludeQualityScore());
        s.setCreatedBy(e.getCreatedBy()); s.setCreatedAt(e.getCreatedAt());
        s.setRevoked(e.isRevoked()); s.setRevokedAt(e.getRevokedAt());
        return s;
    }
    private SbomShareLinkEntity toEntity(SbomShareLink s) {
        SbomShareLinkEntity e = new SbomShareLinkEntity();
        e.setId(s.getId()); e.setOrgId(s.getOrgId()); e.setReleaseId(s.getReleaseId());
        e.setEvidenceId(s.getEvidenceId()); e.setToken(s.getToken());
        e.setRecipientEmail(s.getRecipientEmail()); e.setRecipientOrg(s.getRecipientOrg());
        e.setExpiresAt(s.getExpiresAt()); e.setMaxDownloads(s.getMaxDownloads());
        e.setDownloadCount(s.getDownloadCount()); e.setIncludeVex(s.isIncludeVex());
        e.setIncludeQualityScore(s.isIncludeQualityScore());
        e.setCreatedBy(s.getCreatedBy()); e.setCreatedAt(s.getCreatedAt());
        e.setRevoked(s.isRevoked()); e.setRevokedAt(s.getRevokedAt());
        return e;
    }
}
