package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.FindingDecision;
import com.lexsecura.domain.repository.FindingDecisionRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaFindingDecisionRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FindingDecisionRepositoryAdapter implements FindingDecisionRepository {

    private final JpaFindingDecisionRepository jpa;
    private final PersistenceMapper mapper;

    public FindingDecisionRepositoryAdapter(JpaFindingDecisionRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public FindingDecision save(FindingDecision d) {
        var e = mapper.toEntity(d);
        e = jpa.save(e);
        return mapper.toDomain(e);
    }

    @Override
    public List<FindingDecision> findAllByFindingId(UUID findingId) {
        return jpa.findAllByFindingId(findingId).stream().map(mapper::toDomain).toList();
    }
}
