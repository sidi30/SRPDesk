package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.RiskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskItemRepository {

    RiskItem save(RiskItem item);

    Optional<RiskItem> findById(UUID id);

    List<RiskItem> findAllByRiskAssessmentId(UUID riskAssessmentId);

    void deleteById(UUID id);
}
