package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.OrgSlaSettings;
import com.lexsecura.domain.repository.OrgSlaSettingsRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrgSlaSettingsRepositoryAdapter implements OrgSlaSettingsRepository {

    private final JpaOrgSlaSettingsRepository jpa;
    private final PersistenceMapper mapper;

    public OrgSlaSettingsRepositoryAdapter(JpaOrgSlaSettingsRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public OrgSlaSettings save(OrgSlaSettings s) {
        return mapper.toDomain(jpa.save(mapper.toEntity(s)));
    }

    @Override
    public Optional<OrgSlaSettings> findByOrgId(UUID orgId) {
        return jpa.findByOrgId(orgId).map(mapper::toDomain);
    }
}
