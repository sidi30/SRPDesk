package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.vex.VexStatement;
import com.lexsecura.domain.repository.VexStatementRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaVexStatementRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class VexStatementRepositoryAdapter implements VexStatementRepository {

    private final JpaVexStatementRepository jpa;
    private final PersistenceMapper mapper;

    public VexStatementRepositoryAdapter(JpaVexStatementRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public VexStatement save(VexStatement s) {
        return mapper.toDomain(jpa.save(mapper.toEntity(s)));
    }

    @Override
    public List<VexStatement> findAllByVexDocumentId(UUID vexDocumentId) {
        return jpa.findAllByVexDocumentId(vexDocumentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteAllByVexDocumentId(UUID vexDocumentId) {
        jpa.deleteAllByVexDocumentId(vexDocumentId);
    }
}
