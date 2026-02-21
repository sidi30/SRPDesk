package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.OrgSlaSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaOrgSlaSettingsRepository extends JpaRepository<OrgSlaSettingsEntity, UUID> {

    Optional<OrgSlaSettingsEntity> findByOrgId(UUID orgId);
}
