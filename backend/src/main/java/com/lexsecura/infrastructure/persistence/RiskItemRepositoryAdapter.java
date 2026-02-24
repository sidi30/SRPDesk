package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.RiskItem;
import com.lexsecura.domain.repository.RiskItemRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RiskItemRepositoryAdapter implements RiskItemRepository {

    private final JpaRiskItemRepository jpa;
    private final PersistenceMapper mapper;

    public RiskItemRepositoryAdapter(JpaRiskItemRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public RiskItem save(RiskItem item) {
        return mapper.toDomain(jpa.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<RiskItem> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RiskItem> findAllByRiskAssessmentId(UUID riskAssessmentId) {
        return jpa.findAllByRiskAssessmentId(riskAssessmentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
