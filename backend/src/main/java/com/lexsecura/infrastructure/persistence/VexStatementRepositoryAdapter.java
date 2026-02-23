package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.vex.VexJustification;
import com.lexsecura.domain.model.vex.VexStatement;
import com.lexsecura.domain.model.vex.VexStatus;
import com.lexsecura.domain.repository.VexStatementRepository;
import com.lexsecura.infrastructure.persistence.entity.VexStatementEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaVexStatementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class VexStatementRepositoryAdapter implements VexStatementRepository {

    private final JpaVexStatementRepository jpa;

    public VexStatementRepositoryAdapter(JpaVexStatementRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public VexStatement save(VexStatement s) {
        return toDomain(jpa.save(toEntity(s)));
    }

    @Override
    public List<VexStatement> findAllByVexDocumentId(UUID vexDocumentId) {
        return jpa.findAllByVexDocumentId(vexDocumentId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteAllByVexDocumentId(UUID vexDocumentId) {
        jpa.deleteAllByVexDocumentId(vexDocumentId);
    }

    private VexStatement toDomain(VexStatementEntity e) {
        VexStatement s = new VexStatement();
        s.setId(e.getId());
        s.setVexDocumentId(e.getVexDocumentId());
        s.setFindingId(e.getFindingId());
        s.setDecisionId(e.getDecisionId());
        s.setVulnerabilityId(e.getVulnerabilityId());
        s.setProductId(e.getProductId());
        s.setStatus(VexStatus.valueOf(e.getStatus()));
        if (e.getJustification() != null && !e.getJustification().isBlank()) {
            s.setJustification(VexJustification.valueOf(e.getJustification()));
        }
        s.setImpactStatement(e.getImpactStatement());
        s.setActionStatement(e.getActionStatement());
        s.setCreatedAt(e.getCreatedAt());
        return s;
    }

    private VexStatementEntity toEntity(VexStatement s) {
        VexStatementEntity e = new VexStatementEntity();
        e.setId(s.getId());
        e.setVexDocumentId(s.getVexDocumentId());
        e.setFindingId(s.getFindingId());
        e.setDecisionId(s.getDecisionId());
        e.setVulnerabilityId(s.getVulnerabilityId());
        e.setProductId(s.getProductId());
        e.setStatus(s.getStatus().name());
        if (s.getJustification() != null) {
            e.setJustification(s.getJustification().name());
        }
        e.setImpactStatement(s.getImpactStatement());
        e.setActionStatement(s.getActionStatement());
        e.setCreatedAt(s.getCreatedAt());
        return e;
    }
}
