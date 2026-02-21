package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CraEventParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CraEventParticipantRepository {

    CraEventParticipant save(CraEventParticipant participant);

    List<CraEventParticipant> findAllByCraEventId(UUID craEventId);

    Optional<CraEventParticipant> findByCraEventIdAndUserId(UUID craEventId, UUID userId);

    void deleteByCraEventIdAndUserId(UUID craEventId, UUID userId);
}
