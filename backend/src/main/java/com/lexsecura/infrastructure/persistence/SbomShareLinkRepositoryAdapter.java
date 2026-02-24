package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SbomShareLink;
import com.lexsecura.domain.repository.SbomShareLinkRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaSbomShareLinkRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SbomShareLinkRepositoryAdapter implements SbomShareLinkRepository {

    private final JpaSbomShareLinkRepository jpa;
    private final PersistenceMapper mapper;

    public SbomShareLinkRepositoryAdapter(JpaSbomShareLinkRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public SbomShareLink save(SbomShareLink s) {
        var e = mapper.toEntity(s);
        e = jpa.save(e);
        return mapper.toDomain(e);
    }

    @Override
    public Optional<SbomShareLink> findByToken(String token) {
        return jpa.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<SbomShareLink> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<SbomShareLink> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgId(releaseId, orgId).stream().map(mapper::toDomain).toList();
    }
}
