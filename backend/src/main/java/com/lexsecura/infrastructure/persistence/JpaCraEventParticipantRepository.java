package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.CraEventParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCraEventParticipantRepository extends JpaRepository<CraEventParticipantEntity, UUID> {

    List<CraEventParticipantEntity> findAllByCraEventId(UUID craEventId);

    Optional<CraEventParticipantEntity> findByCraEventIdAndUserId(UUID craEventId, UUID userId);

    void deleteByCraEventIdAndUserId(UUID craEventId, UUID userId);
}
