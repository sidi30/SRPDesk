package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEventParticipant;
import com.lexsecura.domain.repository.CraEventParticipantRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CraEventParticipantRepositoryAdapter implements CraEventParticipantRepository {

    private final JpaCraEventParticipantRepository jpa;
    private final PersistenceMapper mapper;

    public CraEventParticipantRepositoryAdapter(JpaCraEventParticipantRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CraEventParticipant save(CraEventParticipant p) {
        return mapper.toDomain(jpa.save(mapper.toEntity(p)));
    }

    @Override
    public List<CraEventParticipant> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<CraEventParticipant> findByCraEventIdAndUserId(UUID craEventId, UUID userId) {
        return jpa.findByCraEventIdAndUserId(craEventId, userId).map(mapper::toDomain);
    }

    @Override
    public void deleteByCraEventIdAndUserId(UUID craEventId, UUID userId) {
        jpa.deleteByCraEventIdAndUserId(craEventId, userId);
    }
}
