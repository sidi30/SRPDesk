package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.FindingDecision;

import java.util.List;
import java.util.UUID;

public interface FindingDecisionRepository {

    FindingDecision save(FindingDecision decision);

    List<FindingDecision> findAllByFindingId(UUID findingId);
}
