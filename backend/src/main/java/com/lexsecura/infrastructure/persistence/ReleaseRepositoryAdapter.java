package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaReleaseRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReleaseRepositoryAdapter implements ReleaseRepository {

    private final JpaReleaseRepository jpa;
    private final PersistenceMapper mapper;

    public ReleaseRepositoryAdapter(JpaReleaseRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Release save(Release release) {
        var entity = mapper.toEntity(release);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Release> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Release> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<Release> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Release> findAllByProductId(UUID productId) {
        return jpa.findAllByProductId(productId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Release> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Release> findByProductIdAndVersionAndOrgId(UUID productId, String version, UUID orgId) {
        return jpa.findByProductIdAndVersionAndOrgId(productId, version, orgId).map(mapper::toDomain);
    }

    @Override
    public List<Release> findAllWithSupportEndingBefore(Instant deadline) {
        return jpa.findAllBySupportedUntilNotNullAndSupportedUntilBefore(deadline).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
