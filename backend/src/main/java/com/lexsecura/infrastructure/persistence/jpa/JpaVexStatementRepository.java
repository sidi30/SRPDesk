package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.VexStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaVexStatementRepository extends JpaRepository<VexStatementEntity, UUID> {
    List<VexStatementEntity> findAllByVexDocumentId(UUID vexDocumentId);
    void deleteAllByVexDocumentId(UUID vexDocumentId);
}
