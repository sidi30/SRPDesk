package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.OrgSlaSettings;

import java.util.Optional;
import java.util.UUID;

public interface OrgSlaSettingsRepository {

    OrgSlaSettings save(OrgSlaSettings settings);

    Optional<OrgSlaSettings> findByOrgId(UUID orgId);
}
