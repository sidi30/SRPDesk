package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SupplierSbom;
import com.lexsecura.domain.repository.SupplierSbomRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaSupplierSbomRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupplierSbomRepositoryAdapter implements SupplierSbomRepository {

    private final JpaSupplierSbomRepository jpa;
    private final PersistenceMapper mapper;

    public SupplierSbomRepositoryAdapter(JpaSupplierSbomRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public SupplierSbom save(SupplierSbom s) {
        var e = mapper.toEntity(s);
        e = jpa.save(e);
        return mapper.toDomain(e);
    }

    @Override
    public Optional<SupplierSbom> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<SupplierSbom> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgId(releaseId, orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByIdAndOrgId(UUID id, UUID orgId) {
        jpa.deleteByIdAndOrgId(id, orgId);
    }
}
