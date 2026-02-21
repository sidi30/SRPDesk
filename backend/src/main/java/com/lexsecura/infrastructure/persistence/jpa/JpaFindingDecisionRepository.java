package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.FindingDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaFindingDecisionRepository extends JpaRepository<FindingDecisionEntity, UUID> {
    List<FindingDecisionEntity> findAllByFindingId(UUID findingId);
}
