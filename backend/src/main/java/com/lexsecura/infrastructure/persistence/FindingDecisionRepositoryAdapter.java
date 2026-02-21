package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.FindingDecision;
import com.lexsecura.domain.repository.FindingDecisionRepository;
import com.lexsecura.infrastructure.persistence.entity.FindingDecisionEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaFindingDecisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FindingDecisionRepositoryAdapter implements FindingDecisionRepository {

    private final JpaFindingDecisionRepository jpa;

    public FindingDecisionRepositoryAdapter(JpaFindingDecisionRepository jpa) { this.jpa = jpa; }

    @Override
    public FindingDecision save(FindingDecision d) {
        var e = toEntity(d); e = jpa.save(e); return toDomain(e);
    }
    @Override
    public List<FindingDecision> findAllByFindingId(UUID findingId) {
        return jpa.findAllByFindingId(findingId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private FindingDecision toDomain(FindingDecisionEntity e) {
        FindingDecision d = new FindingDecision();
        d.setId(e.getId()); d.setFindingId(e.getFindingId()); d.setDecisionType(e.getDecisionType());
        d.setRationale(e.getRationale()); d.setDueDate(e.getDueDate());
        d.setDecidedBy(e.getDecidedBy()); d.setFixReleaseId(e.getFixReleaseId());
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }
    private FindingDecisionEntity toEntity(FindingDecision d) {
        FindingDecisionEntity e = new FindingDecisionEntity();
        e.setId(d.getId()); e.setFindingId(d.getFindingId()); e.setDecisionType(d.getDecisionType());
        e.setRationale(d.getRationale()); e.setDueDate(d.getDueDate());
        e.setDecidedBy(d.getDecidedBy()); e.setFixReleaseId(d.getFixReleaseId());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }
}
