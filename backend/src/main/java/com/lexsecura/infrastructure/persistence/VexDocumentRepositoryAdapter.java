package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.vex.VexDocument;
import com.lexsecura.domain.repository.VexDocumentRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaVexDocumentRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VexDocumentRepositoryAdapter implements VexDocumentRepository {

    private final JpaVexDocumentRepository jpa;
    private final PersistenceMapper mapper;

    public VexDocumentRepositoryAdapter(JpaVexDocumentRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public VexDocument save(VexDocument doc) {
        return mapper.toDomain(jpa.save(mapper.toEntity(doc)));
    }

    @Override
    public Optional<VexDocument> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<VexDocument> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<VexDocument> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgIdOrderByCreatedAtDesc(releaseId, orgId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
