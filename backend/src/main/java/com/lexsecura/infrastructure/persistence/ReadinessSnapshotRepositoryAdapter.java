package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ReadinessSnapshot;
import com.lexsecura.domain.repository.ReadinessSnapshotRepository;
import com.lexsecura.infrastructure.persistence.entity.ReadinessSnapshotEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaReadinessSnapshotRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ReadinessSnapshotRepositoryAdapter implements ReadinessSnapshotRepository {

    private final JpaReadinessSnapshotRepository jpa;

    public ReadinessSnapshotRepositoryAdapter(JpaReadinessSnapshotRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ReadinessSnapshot save(ReadinessSnapshot snapshot) {
        return toDomain(jpa.save(toEntity(snapshot)));
    }

    @Override
    public List<ReadinessSnapshot> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgIdOrderBySnapshotAtDesc(productId, orgId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ReadinessSnapshot toDomain(ReadinessSnapshotEntity e) {
        ReadinessSnapshot m = new ReadinessSnapshot();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setOverallScore(e.getOverallScore());
        m.setCategoryScoresJson(e.getCategoryScores());
        m.setActionItemsJson(e.getActionItems());
        m.setSnapshotAt(e.getSnapshotAt());
        m.setCreatedBy(e.getCreatedBy());
        return m;
    }

    private ReadinessSnapshotEntity toEntity(ReadinessSnapshot m) {
        ReadinessSnapshotEntity e = new ReadinessSnapshotEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setOverallScore(m.getOverallScore());
        e.setCategoryScores(m.getCategoryScoresJson());
        e.setActionItems(m.getActionItemsJson());
        e.setSnapshotAt(m.getSnapshotAt());
        e.setCreatedBy(m.getCreatedBy());
        return e;
    }
}
