package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.SrpSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSrpSubmissionRepository extends JpaRepository<SrpSubmissionEntity, UUID> {

    Optional<SrpSubmissionEntity> findByIdAndCraEventId(UUID id, UUID craEventId);

    List<SrpSubmissionEntity> findAllByCraEventIdOrderByGeneratedAtDesc(UUID craEventId);
}
