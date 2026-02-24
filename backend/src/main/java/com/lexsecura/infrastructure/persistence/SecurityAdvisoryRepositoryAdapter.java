package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SecurityAdvisory;
import com.lexsecura.domain.repository.SecurityAdvisoryRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaSecurityAdvisoryRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SecurityAdvisoryRepositoryAdapter implements SecurityAdvisoryRepository {

    private final JpaSecurityAdvisoryRepository jpa;
    private final PersistenceMapper mapper;

    public SecurityAdvisoryRepositoryAdapter(JpaSecurityAdvisoryRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public SecurityAdvisory save(SecurityAdvisory a) {
        return mapper.toDomain(jpa.save(mapper.toEntity(a)));
    }

    @Override
    public Optional<SecurityAdvisory> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<SecurityAdvisory> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SecurityAdvisory> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(mapper::toDomain).toList();
    }
}
