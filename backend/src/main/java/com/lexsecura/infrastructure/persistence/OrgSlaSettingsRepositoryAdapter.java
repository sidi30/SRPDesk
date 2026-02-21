package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.OrgSlaSettings;
import com.lexsecura.domain.repository.OrgSlaSettingsRepository;
import com.lexsecura.infrastructure.persistence.entity.OrgSlaSettingsEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrgSlaSettingsRepositoryAdapter implements OrgSlaSettingsRepository {

    private final JpaOrgSlaSettingsRepository jpa;

    public OrgSlaSettingsRepositoryAdapter(JpaOrgSlaSettingsRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrgSlaSettings save(OrgSlaSettings s) {
        return toDomain(jpa.save(toEntity(s)));
    }

    @Override
    public Optional<OrgSlaSettings> findByOrgId(UUID orgId) {
        return jpa.findByOrgId(orgId).map(this::toDomain);
    }

    private OrgSlaSettings toDomain(OrgSlaSettingsEntity e) {
        OrgSlaSettings m = new OrgSlaSettings();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setEarlyWarningHours(e.getEarlyWarningHours());
        m.setNotificationHours(e.getNotificationHours());
        m.setFinalReportDaysAfterPatch(e.getFinalReportDaysAfterPatch());
        m.setFinalReportDaysAfterResolve(e.getFinalReportDaysAfterResolve());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private OrgSlaSettingsEntity toEntity(OrgSlaSettings m) {
        OrgSlaSettingsEntity e = new OrgSlaSettingsEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setEarlyWarningHours(m.getEarlyWarningHours());
        e.setNotificationHours(m.getNotificationHours());
        e.setFinalReportDaysAfterPatch(m.getFinalReportDaysAfterPatch());
        e.setFinalReportDaysAfterResolve(m.getFinalReportDaysAfterResolve());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
