package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CraEventLink;

import java.util.List;
import java.util.UUID;

public interface CraEventLinkRepository {

    CraEventLink save(CraEventLink link);

    List<CraEventLink> findAllByCraEventId(UUID craEventId);

    List<CraEventLink> findAllByCraEventIdAndLinkType(UUID craEventId, String linkType);

    void deleteByCraEventIdAndLinkTypeAndTargetId(UUID craEventId, String linkType, UUID targetId);
}
