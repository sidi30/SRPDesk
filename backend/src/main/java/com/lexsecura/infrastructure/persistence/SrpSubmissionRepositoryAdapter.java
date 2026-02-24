package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SrpSubmission;
import com.lexsecura.domain.repository.SrpSubmissionRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SrpSubmissionRepositoryAdapter implements SrpSubmissionRepository {

    private final JpaSrpSubmissionRepository jpa;
    private final PersistenceMapper mapper;

    public SrpSubmissionRepositoryAdapter(JpaSrpSubmissionRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public SrpSubmission save(SrpSubmission s) {
        return mapper.toDomain(jpa.save(mapper.toEntity(s)));
    }

    @Override
    public Optional<SrpSubmission> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SrpSubmission> findByIdAndCraEventId(UUID id, UUID craEventId) {
        return jpa.findByIdAndCraEventId(id, craEventId).map(mapper::toDomain);
    }

    @Override
    public List<SrpSubmission> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventIdOrderByGeneratedAtDesc(craEventId).stream().map(mapper::toDomain).toList();
    }
}
