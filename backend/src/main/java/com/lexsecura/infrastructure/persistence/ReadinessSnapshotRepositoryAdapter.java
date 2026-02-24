package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ReadinessSnapshot;
import com.lexsecura.domain.repository.ReadinessSnapshotRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaReadinessSnapshotRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ReadinessSnapshotRepositoryAdapter implements ReadinessSnapshotRepository {

    private final JpaReadinessSnapshotRepository jpa;
    private final PersistenceMapper mapper;

    public ReadinessSnapshotRepositoryAdapter(JpaReadinessSnapshotRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ReadinessSnapshot save(ReadinessSnapshot snapshot) {
        return mapper.toDomain(jpa.save(mapper.toEntity(snapshot)));
    }

    @Override
    public List<ReadinessSnapshot> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgIdOrderBySnapshotAtDesc(productId, orgId)
                .stream().map(mapper::toDomain).toList();
    }
}
