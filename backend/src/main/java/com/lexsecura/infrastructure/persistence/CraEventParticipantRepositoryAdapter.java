package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEventParticipant;
import com.lexsecura.domain.repository.CraEventParticipantRepository;
import com.lexsecura.infrastructure.persistence.entity.CraEventParticipantEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CraEventParticipantRepositoryAdapter implements CraEventParticipantRepository {

    private final JpaCraEventParticipantRepository jpa;

    public CraEventParticipantRepositoryAdapter(JpaCraEventParticipantRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CraEventParticipant save(CraEventParticipant p) {
        return toDomain(jpa.save(toEntity(p)));
    }

    @Override
    public List<CraEventParticipant> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<CraEventParticipant> findByCraEventIdAndUserId(UUID craEventId, UUID userId) {
        return jpa.findByCraEventIdAndUserId(craEventId, userId).map(this::toDomain);
    }

    @Override
    public void deleteByCraEventIdAndUserId(UUID craEventId, UUID userId) {
        jpa.deleteByCraEventIdAndUserId(craEventId, userId);
    }

    private CraEventParticipant toDomain(CraEventParticipantEntity e) {
        CraEventParticipant m = new CraEventParticipant();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setUserId(e.getUserId());
        m.setRole(e.getRole());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    private CraEventParticipantEntity toEntity(CraEventParticipant m) {
        CraEventParticipantEntity e = new CraEventParticipantEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setUserId(m.getUserId());
        e.setRole(m.getRole());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
