package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.ReadinessSnapshot;

import java.util.List;
import java.util.UUID;

public interface ReadinessSnapshotRepository {

    ReadinessSnapshot save(ReadinessSnapshot snapshot);

    List<ReadinessSnapshot> findAllByProductIdAndOrgId(UUID productId, UUID orgId);
}
