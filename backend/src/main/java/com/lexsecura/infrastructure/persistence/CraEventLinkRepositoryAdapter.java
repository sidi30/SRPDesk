package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEventLink;
import com.lexsecura.domain.repository.CraEventLinkRepository;
import com.lexsecura.infrastructure.persistence.entity.CraEventLinkEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CraEventLinkRepositoryAdapter implements CraEventLinkRepository {

    private final JpaCraEventLinkRepository jpa;

    public CraEventLinkRepositoryAdapter(JpaCraEventLinkRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CraEventLink save(CraEventLink link) {
        return toDomain(jpa.save(toEntity(link)));
    }

    @Override
    public List<CraEventLink> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CraEventLink> findAllByCraEventIdAndLinkType(UUID craEventId, String linkType) {
        return jpa.findAllByCraEventIdAndLinkType(craEventId, linkType).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByCraEventIdAndLinkTypeAndTargetId(UUID craEventId, String linkType, UUID targetId) {
        jpa.deleteByCraEventIdAndLinkTypeAndTargetId(craEventId, linkType, targetId);
    }

    private CraEventLink toDomain(CraEventLinkEntity e) {
        CraEventLink m = new CraEventLink();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setLinkType(e.getLinkType());
        m.setTargetId(e.getTargetId());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    private CraEventLinkEntity toEntity(CraEventLink m) {
        CraEventLinkEntity e = new CraEventLinkEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setLinkType(m.getLinkType());
        e.setTargetId(m.getTargetId());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
