package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.CraEventLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCraEventLinkRepository extends JpaRepository<CraEventLinkEntity, UUID> {

    List<CraEventLinkEntity> findAllByCraEventId(UUID craEventId);

    List<CraEventLinkEntity> findAllByCraEventIdAndLinkType(UUID craEventId, String linkType);

    void deleteByCraEventIdAndLinkTypeAndTargetId(UUID craEventId, String linkType, UUID targetId);
}
