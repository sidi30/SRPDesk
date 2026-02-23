package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.vex.VexStatement;

import java.util.List;
import java.util.UUID;

public interface VexStatementRepository {
    VexStatement save(VexStatement statement);
    List<VexStatement> findAllByVexDocumentId(UUID vexDocumentId);
    void deleteAllByVexDocumentId(UUID vexDocumentId);
}
