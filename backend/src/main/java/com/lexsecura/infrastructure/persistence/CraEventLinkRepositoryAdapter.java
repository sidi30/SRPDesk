package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEventLink;
import com.lexsecura.domain.repository.CraEventLinkRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CraEventLinkRepositoryAdapter implements CraEventLinkRepository {

    private final JpaCraEventLinkRepository jpa;
    private final PersistenceMapper mapper;

    public CraEventLinkRepositoryAdapter(JpaCraEventLinkRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CraEventLink save(CraEventLink link) {
        return mapper.toDomain(jpa.save(mapper.toEntity(link)));
    }

    @Override
    public List<CraEventLink> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CraEventLink> findAllByCraEventIdAndLinkType(UUID craEventId, String linkType) {
        return jpa.findAllByCraEventIdAndLinkType(craEventId, linkType).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByCraEventIdAndLinkTypeAndTargetId(UUID craEventId, String linkType, UUID targetId) {
        jpa.deleteByCraEventIdAndLinkTypeAndTargetId(craEventId, linkType, targetId);
    }
}
