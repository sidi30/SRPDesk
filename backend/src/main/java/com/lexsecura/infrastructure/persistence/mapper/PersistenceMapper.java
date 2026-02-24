package com.lexsecura.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.vex.VexDocument;
import com.lexsecura.domain.model.vex.VexFormat;
import com.lexsecura.domain.model.vex.VexJustification;
import com.lexsecura.domain.model.vex.VexStatement;
import com.lexsecura.domain.model.vex.VexStatus;
import com.lexsecura.infrastructure.persistence.entity.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Component
public class PersistenceMapper {

    private final ObjectMapper objectMapper;

    public PersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ──────────────────────────────────────────────
    // Product mappings
    // ──────────────────────────────────────────────

    public Product toDomain(ProductEntity entity) {
        Product p = new Product();
        p.setId(entity.getId());
        p.setOrgId(entity.getOrgId());
        p.setName(entity.getName());
        p.setType(entity.getType());
        p.setCriticality(entity.getCriticality());
        p.setContacts(deserializeContacts(entity.getContacts()));
        p.setConformityPath(entity.getConformityPath());
        p.setCreatedAt(entity.getCreatedAt());
        p.setUpdatedAt(entity.getUpdatedAt());
        return p;
    }

    public ProductEntity toEntity(Product domain) {
        ProductEntity e = new ProductEntity();
        e.setId(domain.getId());
        e.setOrgId(domain.getOrgId());
        e.setName(domain.getName());
        e.setType(domain.getType());
        e.setCriticality(domain.getCriticality());
        e.setContacts(serializeContacts(domain.getContacts()));
        e.setConformityPath(domain.getConformityPath());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // Release mappings
    // ──────────────────────────────────────────────

    public Release toDomain(ReleaseEntity entity) {
        Release r = new Release();
        r.setId(entity.getId());
        r.setProductId(entity.getProductId());
        r.setOrgId(entity.getOrgId());
        r.setVersion(entity.getVersion());
        r.setGitRef(entity.getGitRef());
        r.setBuildId(entity.getBuildId());
        r.setReleasedAt(entity.getReleasedAt());
        r.setSupportedUntil(entity.getSupportedUntil());
        r.setStatus(ReleaseStatus.valueOf(entity.getStatus()));
        r.setUpdateType(entity.getUpdateType());
        r.setSecurityImpact(entity.getSecurityImpact());
        r.setCveIds(entity.getCveIds());
        r.setCreatedAt(entity.getCreatedAt());
        r.setUpdatedAt(entity.getUpdatedAt());
        return r;
    }

    public ReleaseEntity toEntity(Release domain) {
        ReleaseEntity e = new ReleaseEntity();
        e.setId(domain.getId());
        e.setProductId(domain.getProductId());
        e.setOrgId(domain.getOrgId());
        e.setVersion(domain.getVersion());
        e.setGitRef(domain.getGitRef());
        e.setBuildId(domain.getBuildId());
        e.setReleasedAt(domain.getReleasedAt());
        e.setSupportedUntil(domain.getSupportedUntil());
        e.setStatus(domain.getStatus().name());
        e.setUpdateType(domain.getUpdateType());
        e.setSecurityImpact(domain.getSecurityImpact());
        e.setCveIds(domain.getCveIds());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // Evidence mappings
    // ──────────────────────────────────────────────

    public Evidence toDomain(EvidenceEntity entity) {
        Evidence ev = new Evidence();
        ev.setId(entity.getId());
        ev.setReleaseId(entity.getReleaseId());
        ev.setOrgId(entity.getOrgId());
        ev.setType(EvidenceType.valueOf(entity.getType()));
        ev.setFilename(entity.getFilename());
        ev.setContentType(entity.getContentType());
        ev.setSize(entity.getSize());
        ev.setSha256(entity.getSha256());
        ev.setStorageUri(entity.getStorageUri());
        ev.setCreatedAt(entity.getCreatedAt());
        ev.setCreatedBy(entity.getCreatedBy());
        return ev;
    }

    public EvidenceEntity toEntity(Evidence domain) {
        EvidenceEntity e = new EvidenceEntity();
        e.setId(domain.getId());
        e.setReleaseId(domain.getReleaseId());
        e.setOrgId(domain.getOrgId());
        e.setType(domain.getType().name());
        e.setFilename(domain.getFilename());
        e.setContentType(domain.getContentType());
        e.setSize(domain.getSize());
        e.setSha256(domain.getSha256());
        e.setStorageUri(domain.getStorageUri());
        e.setCreatedAt(domain.getCreatedAt());
        e.setCreatedBy(domain.getCreatedBy());
        return e;
    }

    // ──────────────────────────────────────────────
    // ApiKey mappings
    // ──────────────────────────────────────────────

    public ApiKey toDomain(ApiKeyEntity entity) {
        ApiKey a = new ApiKey();
        a.setId(entity.getId());
        a.setOrgId(entity.getOrgId());
        a.setName(entity.getName());
        a.setKeyPrefix(entity.getKeyPrefix());
        a.setKeyHash(entity.getKeyHash());
        a.setScopes(entity.getScopes());
        a.setCreatedBy(entity.getCreatedBy());
        a.setCreatedAt(entity.getCreatedAt());
        a.setLastUsedAt(entity.getLastUsedAt());
        a.setRevoked(entity.isRevoked());
        a.setRevokedAt(entity.getRevokedAt());
        return a;
    }

    public ApiKeyEntity toEntity(ApiKey domain) {
        ApiKeyEntity e = new ApiKeyEntity();
        e.setId(domain.getId());
        e.setOrgId(domain.getOrgId());
        e.setName(domain.getName());
        e.setKeyPrefix(domain.getKeyPrefix());
        e.setKeyHash(domain.getKeyHash());
        e.setScopes(domain.getScopes());
        e.setCreatedBy(domain.getCreatedBy());
        e.setCreatedAt(domain.getCreatedAt());
        e.setLastUsedAt(domain.getLastUsedAt());
        e.setRevoked(domain.isRevoked());
        e.setRevokedAt(domain.getRevokedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // AuditEvent mappings
    // ──────────────────────────────────────────────

    public AuditEvent toDomain(AuditEventEntity entity) {
        AuditEvent a = new AuditEvent();
        a.setId(entity.getId());
        a.setOrgId(entity.getOrgId());
        a.setEntityType(entity.getEntityType());
        a.setEntityId(entity.getEntityId());
        a.setAction(entity.getAction());
        a.setActor(entity.getActor());
        a.setPayloadJson(entity.getPayloadJson());
        a.setCreatedAt(entity.getCreatedAt());
        a.setPrevHash(entity.getPrevHash());
        a.setHash(entity.getHash());
        return a;
    }

    public AuditEventEntity toEntity(AuditEvent domain) {
        AuditEventEntity e = new AuditEventEntity();
        e.setId(domain.getId());
        e.setOrgId(domain.getOrgId());
        e.setEntityType(domain.getEntityType());
        e.setEntityId(domain.getEntityId());
        e.setAction(domain.getAction());
        e.setActor(domain.getActor());
        e.setPayloadJson(domain.getPayloadJson());
        e.setCreatedAt(domain.getCreatedAt());
        e.setPrevHash(domain.getPrevHash());
        e.setHash(domain.getHash());
        return e;
    }

    // ──────────────────────────────────────────────
    // Organization mappings
    // ──────────────────────────────────────────────

    public Organization toDomain(OrganizationEntity entity) {
        Organization org = new Organization();
        org.setId(entity.getId());
        org.setName(entity.getName());
        org.setSlug(entity.getSlug());
        org.setCreatedAt(entity.getCreatedAt());
        org.setUpdatedAt(entity.getUpdatedAt());
        return org;
    }

    public OrganizationEntity toEntity(Organization domain) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    // ──────────────────────────────────────────────
    // OrgMember mappings
    // ──────────────────────────────────────────────

    public OrgMember toDomain(OrgMemberEntity entity) {
        OrgMember member = new OrgMember();
        member.setId(entity.getId());
        member.setOrgId(entity.getOrgId());
        member.setUserId(entity.getUserId());
        member.setEmail(entity.getEmail());
        member.setRole(entity.getRole());
        member.setJoinedAt(entity.getJoinedAt());
        return member;
    }

    public OrgMemberEntity toEntity(OrgMember domain) {
        OrgMemberEntity entity = new OrgMemberEntity();
        entity.setId(domain.getId());
        entity.setOrgId(domain.getOrgId());
        entity.setUserId(domain.getUserId());
        entity.setEmail(domain.getEmail());
        entity.setRole(domain.getRole());
        entity.setJoinedAt(domain.getJoinedAt());
        return entity;
    }

    // ──────────────────────────────────────────────
    // OrgSlaSettings mappings
    // ──────────────────────────────────────────────

    public OrgSlaSettings toDomain(OrgSlaSettingsEntity e) {
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

    public OrgSlaSettingsEntity toEntity(OrgSlaSettings m) {
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

    // ──────────────────────────────────────────────
    // CraEvent mappings
    // ──────────────────────────────────────────────

    public CraEvent toDomain(CraEventEntity e) {
        CraEvent m = new CraEvent();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setEventType(e.getEventType());
        m.setTitle(e.getTitle());
        m.setDescription(e.getDescription());
        m.setStatus(e.getStatus());
        m.setStartedAt(e.getStartedAt());
        m.setDetectedAt(e.getDetectedAt());
        m.setPatchAvailableAt(e.getPatchAvailableAt());
        m.setResolvedAt(e.getResolvedAt());
        m.setEscalationLevel(e.getEscalationLevel());
        m.setEscalatedAt(e.getEscalatedAt());
        m.setAutoSubmitted(e.getAutoSubmitted() != null && e.getAutoSubmitted());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public CraEventEntity toEntity(CraEvent m) {
        CraEventEntity e = new CraEventEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setEventType(m.getEventType());
        e.setTitle(m.getTitle());
        e.setDescription(m.getDescription());
        e.setStatus(m.getStatus());
        e.setStartedAt(m.getStartedAt());
        e.setDetectedAt(m.getDetectedAt());
        e.setPatchAvailableAt(m.getPatchAvailableAt());
        e.setResolvedAt(m.getResolvedAt());
        e.setEscalationLevel(m.getEscalationLevel());
        e.setEscalatedAt(m.getEscalatedAt());
        e.setAutoSubmitted(m.isAutoSubmitted());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // CraEventParticipant mappings
    // ──────────────────────────────────────────────

    public CraEventParticipant toDomain(CraEventParticipantEntity e) {
        CraEventParticipant m = new CraEventParticipant();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setUserId(e.getUserId());
        m.setRole(e.getRole());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    public CraEventParticipantEntity toEntity(CraEventParticipant m) {
        CraEventParticipantEntity e = new CraEventParticipantEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setUserId(m.getUserId());
        e.setRole(m.getRole());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // CraEventLink mappings
    // ──────────────────────────────────────────────

    public CraEventLink toDomain(CraEventLinkEntity e) {
        CraEventLink m = new CraEventLink();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setLinkType(e.getLinkType());
        m.setTargetId(e.getTargetId());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    public CraEventLinkEntity toEntity(CraEventLink m) {
        CraEventLinkEntity e = new CraEventLinkEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setLinkType(m.getLinkType());
        e.setTargetId(m.getTargetId());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // CraChecklistItem mappings
    // ──────────────────────────────────────────────

    public CraChecklistItem toDomain(CraChecklistItemEntity e) {
        CraChecklistItem m = new CraChecklistItem();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setRequirementRef(e.getRequirementRef());
        m.setCategory(e.getCategory());
        m.setTitle(e.getTitle());
        m.setDescription(e.getDescription());
        m.setStatus(e.getStatus());
        m.setEvidenceIds(e.getEvidenceIds() != null ? Arrays.asList(e.getEvidenceIds()) : List.of());
        m.setNotes(e.getNotes());
        m.setAssessedBy(e.getAssessedBy());
        m.setAssessedAt(e.getAssessedAt());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public CraChecklistItemEntity toEntity(CraChecklistItem m) {
        CraChecklistItemEntity e = new CraChecklistItemEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setRequirementRef(m.getRequirementRef());
        e.setCategory(m.getCategory());
        e.setTitle(m.getTitle());
        e.setDescription(m.getDescription());
        e.setStatus(m.getStatus());
        e.setEvidenceIds(m.getEvidenceIds() != null ? m.getEvidenceIds().toArray(new UUID[0]) : new UUID[0]);
        e.setNotes(m.getNotes());
        e.setAssessedBy(m.getAssessedBy());
        e.setAssessedAt(m.getAssessedAt());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // ReadinessSnapshot mappings
    // ──────────────────────────────────────────────

    public ReadinessSnapshot toDomain(ReadinessSnapshotEntity e) {
        ReadinessSnapshot m = new ReadinessSnapshot();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setOverallScore(e.getOverallScore());
        m.setCategoryScoresJson(e.getCategoryScores());
        m.setActionItemsJson(e.getActionItems());
        m.setSnapshotAt(e.getSnapshotAt());
        m.setCreatedBy(e.getCreatedBy());
        return m;
    }

    public ReadinessSnapshotEntity toEntity(ReadinessSnapshot m) {
        ReadinessSnapshotEntity e = new ReadinessSnapshotEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setOverallScore(m.getOverallScore());
        e.setCategoryScores(m.getCategoryScoresJson());
        e.setActionItems(m.getActionItemsJson());
        e.setSnapshotAt(m.getSnapshotAt());
        e.setCreatedBy(m.getCreatedBy());
        return e;
    }

    // ──────────────────────────────────────────────
    // Finding mappings
    // ──────────────────────────────────────────────

    public Finding toDomain(FindingEntity e) {
        Finding f = new Finding();
        f.setId(e.getId());
        f.setReleaseId(e.getReleaseId());
        f.setComponentId(e.getComponentId());
        f.setVulnerabilityId(e.getVulnerabilityId());
        f.setStatus(e.getStatus());
        f.setDetectedAt(e.getDetectedAt());
        f.setSource(e.getSource());
        return f;
    }

    public FindingEntity toEntity(Finding f) {
        FindingEntity e = new FindingEntity();
        e.setId(f.getId());
        e.setReleaseId(f.getReleaseId());
        e.setComponentId(f.getComponentId());
        e.setVulnerabilityId(f.getVulnerabilityId());
        e.setStatus(f.getStatus());
        e.setDetectedAt(f.getDetectedAt());
        e.setSource(f.getSource());
        return e;
    }

    // ──────────────────────────────────────────────
    // FindingDecision mappings
    // ──────────────────────────────────────────────

    public FindingDecision toDomain(FindingDecisionEntity e) {
        FindingDecision d = new FindingDecision();
        d.setId(e.getId());
        d.setFindingId(e.getFindingId());
        d.setDecisionType(e.getDecisionType());
        d.setRationale(e.getRationale());
        d.setDueDate(e.getDueDate());
        d.setDecidedBy(e.getDecidedBy());
        d.setFixReleaseId(e.getFixReleaseId());
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }

    public FindingDecisionEntity toEntity(FindingDecision d) {
        FindingDecisionEntity e = new FindingDecisionEntity();
        e.setId(d.getId());
        e.setFindingId(d.getFindingId());
        e.setDecisionType(d.getDecisionType());
        e.setRationale(d.getRationale());
        e.setDueDate(d.getDueDate());
        e.setDecidedBy(d.getDecidedBy());
        e.setFixReleaseId(d.getFixReleaseId());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // Component mappings
    // ──────────────────────────────────────────────

    public Component toDomain(ComponentEntity e) {
        Component c = new Component();
        c.setId(e.getId());
        c.setPurl(e.getPurl());
        c.setName(e.getName());
        c.setVersion(e.getVersion());
        c.setType(e.getType());
        c.setLicense(e.getLicense());
        c.setSupplier(e.getSupplier());
        return c;
    }

    public ComponentEntity toEntity(Component c) {
        ComponentEntity e = new ComponentEntity();
        e.setId(c.getId());
        e.setPurl(c.getPurl());
        e.setName(c.getName());
        e.setVersion(c.getVersion());
        e.setType(c.getType());
        e.setLicense(c.getLicense());
        e.setSupplier(c.getSupplier());
        return e;
    }

    // ──────────────────────────────────────────────
    // ReleaseComponent mappings
    // ──────────────────────────────────────────────

    public ReleaseComponent toDomain(ReleaseComponentEntity e) {
        ReleaseComponent rc = new ReleaseComponent();
        rc.setId(e.getId());
        rc.setReleaseId(e.getReleaseId());
        rc.setComponentId(e.getComponentId());
        rc.setCreatedAt(e.getCreatedAt());
        return rc;
    }

    public ReleaseComponentEntity toEntity(ReleaseComponent rc) {
        ReleaseComponentEntity e = new ReleaseComponentEntity();
        e.setId(rc.getId());
        e.setReleaseId(rc.getReleaseId());
        e.setComponentId(rc.getComponentId());
        e.setCreatedAt(rc.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // Vulnerability mappings
    // ──────────────────────────────────────────────

    public Vulnerability toDomain(VulnerabilityEntity e) {
        Vulnerability v = new Vulnerability();
        v.setId(e.getId());
        v.setOsvId(e.getOsvId());
        v.setSummary(e.getSummary());
        v.setDetails(e.getDetails());
        v.setSeverity(e.getSeverity());
        v.setPublished(e.getPublished());
        v.setModified(e.getModified());
        v.setAliases(e.getAliases());
        v.setCreatedAt(e.getCreatedAt());
        v.setActivelyExploited(e.isActivelyExploited());
        v.setKevDateAdded(e.getKevDateAdded());
        v.setEuvdId(e.getEuvdId());
        v.setCvssScore(e.getCvssScore());
        v.setCvssVector(e.getCvssVector());
        return v;
    }

    public VulnerabilityEntity toEntity(Vulnerability v) {
        VulnerabilityEntity e = new VulnerabilityEntity();
        e.setId(v.getId());
        e.setOsvId(v.getOsvId());
        e.setSummary(v.getSummary());
        e.setDetails(v.getDetails());
        e.setSeverity(v.getSeverity());
        e.setPublished(v.getPublished());
        e.setModified(v.getModified());
        e.setAliases(v.getAliases());
        e.setCreatedAt(v.getCreatedAt());
        e.setActivelyExploited(v.isActivelyExploited());
        e.setKevDateAdded(v.getKevDateAdded());
        e.setEuvdId(v.getEuvdId());
        e.setCvssScore(v.getCvssScore());
        e.setCvssVector(v.getCvssVector());
        return e;
    }

    // ──────────────────────────────────────────────
    // VexDocument mappings
    // ──────────────────────────────────────────────

    public VexDocument toDomain(VexDocumentEntity e) {
        VexDocument d = new VexDocument();
        d.setId(e.getId());
        d.setOrgId(e.getOrgId());
        d.setReleaseId(e.getReleaseId());
        d.setFormat(VexFormat.valueOf(e.getFormat()));
        d.setVersion(e.getVersion());
        d.setStatus(e.getStatus());
        d.setDocumentJson(e.getDocumentJson());
        d.setSha256Hash(e.getSha256Hash());
        d.setGeneratedBy(e.getGeneratedBy());
        d.setPublishedAt(e.getPublishedAt());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public VexDocumentEntity toEntity(VexDocument d) {
        VexDocumentEntity e = new VexDocumentEntity();
        e.setId(d.getId());
        e.setOrgId(d.getOrgId());
        e.setReleaseId(d.getReleaseId());
        e.setFormat(d.getFormat().name());
        e.setVersion(d.getVersion());
        e.setStatus(d.getStatus());
        e.setDocumentJson(d.getDocumentJson());
        e.setSha256Hash(d.getSha256Hash());
        e.setGeneratedBy(d.getGeneratedBy());
        e.setPublishedAt(d.getPublishedAt());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // VexStatement mappings
    // ──────────────────────────────────────────────

    public VexStatement toDomain(VexStatementEntity e) {
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

    public VexStatementEntity toEntity(VexStatement s) {
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

    // ──────────────────────────────────────────────
    // SbomShareLink mappings
    // ──────────────────────────────────────────────

    public SbomShareLink toDomain(SbomShareLinkEntity e) {
        SbomShareLink s = new SbomShareLink();
        s.setId(e.getId());
        s.setOrgId(e.getOrgId());
        s.setReleaseId(e.getReleaseId());
        s.setEvidenceId(e.getEvidenceId());
        s.setToken(e.getToken());
        s.setRecipientEmail(e.getRecipientEmail());
        s.setRecipientOrg(e.getRecipientOrg());
        s.setExpiresAt(e.getExpiresAt());
        s.setMaxDownloads(e.getMaxDownloads());
        s.setDownloadCount(e.getDownloadCount());
        s.setIncludeVex(e.isIncludeVex());
        s.setIncludeQualityScore(e.isIncludeQualityScore());
        s.setCreatedBy(e.getCreatedBy());
        s.setCreatedAt(e.getCreatedAt());
        s.setRevoked(e.isRevoked());
        s.setRevokedAt(e.getRevokedAt());
        return s;
    }

    public SbomShareLinkEntity toEntity(SbomShareLink s) {
        SbomShareLinkEntity e = new SbomShareLinkEntity();
        e.setId(s.getId());
        e.setOrgId(s.getOrgId());
        e.setReleaseId(s.getReleaseId());
        e.setEvidenceId(s.getEvidenceId());
        e.setToken(s.getToken());
        e.setRecipientEmail(s.getRecipientEmail());
        e.setRecipientOrg(s.getRecipientOrg());
        e.setExpiresAt(s.getExpiresAt());
        e.setMaxDownloads(s.getMaxDownloads());
        e.setDownloadCount(s.getDownloadCount());
        e.setIncludeVex(s.isIncludeVex());
        e.setIncludeQualityScore(s.isIncludeQualityScore());
        e.setCreatedBy(s.getCreatedBy());
        e.setCreatedAt(s.getCreatedAt());
        e.setRevoked(s.isRevoked());
        e.setRevokedAt(s.getRevokedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // SupplierSbom mappings
    // ──────────────────────────────────────────────

    public SupplierSbom toDomain(SupplierSbomEntity e) {
        SupplierSbom s = new SupplierSbom();
        s.setId(e.getId());
        s.setOrgId(e.getOrgId());
        s.setReleaseId(e.getReleaseId());
        s.setSupplierName(e.getSupplierName());
        s.setSupplierUrl(e.getSupplierUrl());
        s.setEvidenceId(e.getEvidenceId());
        s.setComponentCount(e.getComponentCount());
        s.setFormat(e.getFormat());
        s.setImportedAt(e.getImportedAt());
        s.setImportedBy(e.getImportedBy());
        return s;
    }

    public SupplierSbomEntity toEntity(SupplierSbom s) {
        SupplierSbomEntity e = new SupplierSbomEntity();
        e.setId(s.getId());
        e.setOrgId(s.getOrgId());
        e.setReleaseId(s.getReleaseId());
        e.setSupplierName(s.getSupplierName());
        e.setSupplierUrl(s.getSupplierUrl());
        e.setEvidenceId(s.getEvidenceId());
        e.setComponentCount(s.getComponentCount());
        e.setFormat(s.getFormat());
        e.setImportedAt(s.getImportedAt());
        e.setImportedBy(s.getImportedBy());
        return e;
    }

    // ──────────────────────────────────────────────
    // SrpSubmission mappings
    // ──────────────────────────────────────────────

    public SrpSubmission toDomain(SrpSubmissionEntity e) {
        SrpSubmission m = new SrpSubmission();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setSubmissionType(e.getSubmissionType());
        m.setStatus(e.getStatus());
        m.setContentJson(e.getContentJson());
        m.setSchemaVersion(e.getSchemaVersion());
        m.setValidationErrors(e.getValidationErrors());
        m.setSubmittedReference(e.getSubmittedReference());
        m.setSubmittedAt(e.getSubmittedAt());
        m.setAcknowledgmentEvidenceId(e.getAcknowledgmentEvidenceId());
        m.setEnisaReference(e.getEnisaReference());
        m.setEnisaSubmittedAt(e.getEnisaSubmittedAt());
        m.setEnisaStatus(e.getEnisaStatus());
        m.setRetryCount(e.getRetryCount() != null ? e.getRetryCount() : 0);
        m.setLastError(e.getLastError());
        m.setCsirtReference(e.getCsirtReference());
        m.setCsirtSubmittedAt(e.getCsirtSubmittedAt());
        m.setCsirtStatus(e.getCsirtStatus());
        m.setCsirtCountryCode(e.getCsirtCountryCode());
        m.setGeneratedBy(e.getGeneratedBy());
        m.setGeneratedAt(e.getGeneratedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public SrpSubmissionEntity toEntity(SrpSubmission m) {
        SrpSubmissionEntity e = new SrpSubmissionEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setSubmissionType(m.getSubmissionType());
        e.setStatus(m.getStatus());
        e.setContentJson(m.getContentJson());
        e.setSchemaVersion(m.getSchemaVersion());
        e.setValidationErrors(m.getValidationErrors());
        e.setSubmittedReference(m.getSubmittedReference());
        e.setSubmittedAt(m.getSubmittedAt());
        e.setAcknowledgmentEvidenceId(m.getAcknowledgmentEvidenceId());
        e.setEnisaReference(m.getEnisaReference());
        e.setEnisaSubmittedAt(m.getEnisaSubmittedAt());
        e.setEnisaStatus(m.getEnisaStatus());
        e.setRetryCount(m.getRetryCount());
        e.setLastError(m.getLastError());
        e.setCsirtReference(m.getCsirtReference());
        e.setCsirtSubmittedAt(m.getCsirtSubmittedAt());
        e.setCsirtStatus(m.getCsirtStatus());
        e.setCsirtCountryCode(m.getCsirtCountryCode());
        e.setGeneratedBy(m.getGeneratedBy());
        e.setGeneratedAt(m.getGeneratedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // CvdPolicy mappings
    // ──────────────────────────────────────────────

    public CvdPolicy toDomain(CvdPolicyEntity e) {
        CvdPolicy m = new CvdPolicy();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setContactEmail(e.getContactEmail());
        m.setContactUrl(e.getContactUrl());
        m.setPgpKeyUrl(e.getPgpKeyUrl());
        m.setPolicyUrl(e.getPolicyUrl());
        m.setDisclosureTimelineDays(e.getDisclosureTimelineDays());
        m.setAcceptsAnonymous(e.isAcceptsAnonymous());
        m.setBugBountyUrl(e.getBugBountyUrl());
        m.setAcceptedLanguages(e.getAcceptedLanguages());
        m.setScopeDescription(e.getScopeDescription());
        m.setStatus(e.getStatus());
        m.setPublishedAt(e.getPublishedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public CvdPolicyEntity toEntity(CvdPolicy m) {
        CvdPolicyEntity e = new CvdPolicyEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setContactEmail(m.getContactEmail());
        e.setContactUrl(m.getContactUrl());
        e.setPgpKeyUrl(m.getPgpKeyUrl());
        e.setPolicyUrl(m.getPolicyUrl());
        e.setDisclosureTimelineDays(m.getDisclosureTimelineDays());
        e.setAcceptsAnonymous(m.isAcceptsAnonymous());
        e.setBugBountyUrl(m.getBugBountyUrl());
        e.setAcceptedLanguages(m.getAcceptedLanguages());
        e.setScopeDescription(m.getScopeDescription());
        e.setStatus(m.getStatus());
        e.setPublishedAt(m.getPublishedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // EuDeclarationOfConformity mappings
    // ──────────────────────────────────────────────

    public EuDeclarationOfConformity toDomain(EuDocEntity e) {
        EuDeclarationOfConformity m = new EuDeclarationOfConformity();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setDeclarationNumber(e.getDeclarationNumber());
        m.setManufacturerName(e.getManufacturerName());
        m.setManufacturerAddress(e.getManufacturerAddress());
        m.setAuthorizedRepName(e.getAuthorizedRepName());
        m.setAuthorizedRepAddress(e.getAuthorizedRepAddress());
        m.setProductName(e.getProductName());
        m.setProductIdentification(e.getProductIdentification());
        m.setConformityAssessmentModule(e.getConformityAssessmentModule());
        m.setNotifiedBodyName(e.getNotifiedBodyName());
        m.setNotifiedBodyNumber(e.getNotifiedBodyNumber());
        m.setNotifiedBodyCertificate(e.getNotifiedBodyCertificate());
        m.setHarmonisedStandards(e.getHarmonisedStandards());
        m.setAdditionalInfo(e.getAdditionalInfo());
        m.setDeclarationText(e.getDeclarationText());
        m.setSignedBy(e.getSignedBy());
        m.setSignedRole(e.getSignedRole());
        m.setSignedAt(e.getSignedAt());
        m.setStatus(e.getStatus());
        m.setPublishedAt(e.getPublishedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public EuDocEntity toEntity(EuDeclarationOfConformity m) {
        EuDocEntity e = new EuDocEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setDeclarationNumber(m.getDeclarationNumber());
        e.setManufacturerName(m.getManufacturerName());
        e.setManufacturerAddress(m.getManufacturerAddress());
        e.setAuthorizedRepName(m.getAuthorizedRepName());
        e.setAuthorizedRepAddress(m.getAuthorizedRepAddress());
        e.setProductName(m.getProductName());
        e.setProductIdentification(m.getProductIdentification());
        e.setConformityAssessmentModule(m.getConformityAssessmentModule());
        e.setNotifiedBodyName(m.getNotifiedBodyName());
        e.setNotifiedBodyNumber(m.getNotifiedBodyNumber());
        e.setNotifiedBodyCertificate(m.getNotifiedBodyCertificate());
        e.setHarmonisedStandards(m.getHarmonisedStandards());
        e.setAdditionalInfo(m.getAdditionalInfo());
        e.setDeclarationText(m.getDeclarationText());
        e.setSignedBy(m.getSignedBy());
        e.setSignedRole(m.getSignedRole());
        e.setSignedAt(m.getSignedAt());
        e.setStatus(m.getStatus());
        e.setPublishedAt(m.getPublishedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // ConformityAssessment mappings
    // ──────────────────────────────────────────────

    public ConformityAssessment toDomain(ConformityAssessmentEntity e) {
        ConformityAssessment m = new ConformityAssessment();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setModule(e.getModule());
        m.setStatus(e.getStatus());
        m.setCurrentStep(e.getCurrentStep());
        m.setTotalSteps(e.getTotalSteps());
        m.setStepsData(e.getStepsData());
        m.setStartedAt(e.getStartedAt());
        m.setCompletedAt(e.getCompletedAt());
        m.setApprovedBy(e.getApprovedBy());
        m.setApprovedAt(e.getApprovedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public ConformityAssessmentEntity toEntity(ConformityAssessment m) {
        ConformityAssessmentEntity e = new ConformityAssessmentEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setModule(m.getModule());
        e.setStatus(m.getStatus());
        e.setCurrentStep(m.getCurrentStep());
        e.setTotalSteps(m.getTotalSteps());
        e.setStepsData(m.getStepsData());
        e.setStartedAt(m.getStartedAt());
        e.setCompletedAt(m.getCompletedAt());
        e.setApprovedBy(m.getApprovedBy());
        e.setApprovedAt(m.getApprovedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // RiskAssessment mappings
    // ──────────────────────────────────────────────

    public RiskAssessment toDomain(RiskAssessmentEntity e) {
        RiskAssessment m = new RiskAssessment();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setTitle(e.getTitle());
        m.setMethodology(e.getMethodology());
        m.setStatus(e.getStatus());
        m.setOverallRiskLevel(e.getOverallRiskLevel());
        m.setSummary(e.getSummary());
        m.setApprovedBy(e.getApprovedBy());
        m.setApprovedAt(e.getApprovedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public RiskAssessmentEntity toEntity(RiskAssessment m) {
        RiskAssessmentEntity e = new RiskAssessmentEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setTitle(m.getTitle());
        e.setMethodology(m.getMethodology());
        e.setStatus(m.getStatus());
        e.setOverallRiskLevel(m.getOverallRiskLevel());
        e.setSummary(m.getSummary());
        e.setApprovedBy(m.getApprovedBy());
        e.setApprovedAt(m.getApprovedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // RiskItem mappings
    // ──────────────────────────────────────────────

    public RiskItem toDomain(RiskItemEntity e) {
        RiskItem m = new RiskItem();
        m.setId(e.getId());
        m.setRiskAssessmentId(e.getRiskAssessmentId());
        m.setThreatCategory(e.getThreatCategory());
        m.setThreatDescription(e.getThreatDescription());
        m.setAffectedAsset(e.getAffectedAsset());
        m.setLikelihood(e.getLikelihood());
        m.setImpact(e.getImpact());
        m.setRiskLevel(e.getRiskLevel());
        m.setExistingControls(e.getExistingControls());
        m.setMitigationPlan(e.getMitigationPlan());
        m.setMitigationStatus(e.getMitigationStatus());
        m.setResidualRiskLevel(e.getResidualRiskLevel());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public RiskItemEntity toEntity(RiskItem m) {
        RiskItemEntity e = new RiskItemEntity();
        e.setId(m.getId());
        e.setRiskAssessmentId(m.getRiskAssessmentId());
        e.setThreatCategory(m.getThreatCategory());
        e.setThreatDescription(m.getThreatDescription());
        e.setAffectedAsset(m.getAffectedAsset());
        e.setLikelihood(m.getLikelihood());
        e.setImpact(m.getImpact());
        e.setRiskLevel(m.getRiskLevel());
        e.setExistingControls(m.getExistingControls());
        e.setMitigationPlan(m.getMitigationPlan());
        e.setMitigationStatus(m.getMitigationStatus());
        e.setResidualRiskLevel(m.getResidualRiskLevel());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // AppliedStandard mappings
    // ──────────────────────────────────────────────

    public AppliedStandard toDomain(AppliedStandardEntity e) {
        AppliedStandard m = new AppliedStandard();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setStandardCode(e.getStandardCode());
        m.setStandardTitle(e.getStandardTitle());
        m.setVersion(e.getVersion());
        m.setComplianceStatus(e.getComplianceStatus());
        m.setNotes(e.getNotes());
        m.setEvidenceIds(e.getEvidenceIds());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    public AppliedStandardEntity toEntity(AppliedStandard m) {
        AppliedStandardEntity e = new AppliedStandardEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setStandardCode(m.getStandardCode());
        e.setStandardTitle(m.getStandardTitle());
        e.setVersion(m.getVersion());
        e.setComplianceStatus(m.getComplianceStatus());
        e.setNotes(m.getNotes());
        e.setEvidenceIds(m.getEvidenceIds());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // AiJob mappings
    // ──────────────────────────────────────────────

    public AiJob toDomain(AiJobEntity e) {
        AiJob m = new AiJob();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setJobType(e.getJobType());
        m.setStatus(e.getStatus());
        m.setModel(e.getModel());
        m.setParamsJson(e.getParamsJson());
        m.setInputHash(e.getInputHash());
        m.setOutputHash(e.getOutputHash());
        m.setError(e.getError());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setCompletedAt(e.getCompletedAt());
        return m;
    }

    public AiJobEntity toEntity(AiJob m) {
        AiJobEntity e = new AiJobEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setJobType(m.getJobType());
        e.setStatus(m.getStatus());
        e.setModel(m.getModel());
        e.setParamsJson(m.getParamsJson());
        e.setInputHash(m.getInputHash());
        e.setOutputHash(m.getOutputHash());
        e.setError(m.getError());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setCompletedAt(m.getCompletedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // AiArtifact mappings
    // ──────────────────────────────────────────────

    public AiArtifact toDomain(AiArtifactEntity e) {
        AiArtifact m = new AiArtifact();
        m.setId(e.getId());
        m.setAiJobId(e.getAiJobId());
        m.setKind(e.getKind());
        m.setContentJson(e.getContentJson());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    public AiArtifactEntity toEntity(AiArtifact m) {
        AiArtifactEntity e = new AiArtifactEntity();
        e.setId(m.getId());
        e.setAiJobId(m.getAiJobId());
        e.setKind(m.getKind());
        e.setContentJson(m.getContentJson());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // Webhook mappings
    // ──────────────────────────────────────────────

    public Webhook toDomain(WebhookEntity e) {
        Webhook w = new Webhook();
        w.setId(e.getId());
        w.setOrgId(e.getOrgId());
        w.setName(e.getName());
        w.setUrl(e.getUrl());
        w.setSecret(e.getSecret());
        w.setEventTypes(e.getEventTypes());
        w.setChannelType(e.getChannelType());
        w.setEnabled(e.isEnabled());
        w.setCreatedBy(e.getCreatedBy());
        w.setCreatedAt(e.getCreatedAt());
        w.setUpdatedAt(e.getUpdatedAt());
        return w;
    }

    public WebhookEntity toEntity(Webhook w) {
        WebhookEntity e = new WebhookEntity();
        e.setId(w.getId());
        e.setOrgId(w.getOrgId());
        e.setName(w.getName());
        e.setUrl(w.getUrl());
        e.setSecret(w.getSecret());
        e.setEventTypes(w.getEventTypes());
        e.setChannelType(w.getChannelType());
        e.setEnabled(w.isEnabled());
        e.setCreatedBy(w.getCreatedBy());
        e.setCreatedAt(w.getCreatedAt());
        e.setUpdatedAt(w.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // WebhookDelivery mappings
    // ──────────────────────────────────────────────

    public WebhookDelivery toDomain(WebhookDeliveryEntity e) {
        WebhookDelivery d = new WebhookDelivery();
        d.setId(e.getId());
        d.setWebhookId(e.getWebhookId());
        d.setEventType(e.getEventType());
        d.setPayload(e.getPayload());
        d.setHttpStatus(e.getHttpStatus());
        d.setResponseBody(e.getResponseBody());
        d.setSuccess(e.isSuccess());
        d.setAttempt(e.getAttempt());
        d.setDeliveredAt(e.getDeliveredAt());
        return d;
    }

    public WebhookDeliveryEntity toEntity(WebhookDelivery d) {
        WebhookDeliveryEntity e = new WebhookDeliveryEntity();
        e.setId(d.getId());
        e.setWebhookId(d.getWebhookId());
        e.setEventType(d.getEventType());
        e.setPayload(d.getPayload());
        e.setHttpStatus(d.getHttpStatus());
        e.setResponseBody(d.getResponseBody());
        e.setSuccess(d.isSuccess());
        e.setAttempt(d.getAttempt());
        e.setDeliveredAt(d.getDeliveredAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // SecurityAdvisory mappings
    // ──────────────────────────────────────────────

    public SecurityAdvisory toDomain(SecurityAdvisoryEntity e) {
        SecurityAdvisory a = new SecurityAdvisory();
        a.setId(e.getId());
        a.setOrgId(e.getOrgId());
        a.setCraEventId(e.getCraEventId());
        a.setProductId(e.getProductId());
        a.setTitle(e.getTitle());
        a.setSeverity(e.getSeverity());
        a.setAffectedVersions(e.getAffectedVersions());
        a.setDescription(e.getDescription());
        a.setRemediation(e.getRemediation());
        a.setAdvisoryUrl(e.getAdvisoryUrl());
        a.setStatus(e.getStatus());
        a.setPublishedAt(e.getPublishedAt());
        a.setNotifiedAt(e.getNotifiedAt());
        a.setCreatedBy(e.getCreatedBy());
        a.setCreatedAt(e.getCreatedAt());
        a.setUpdatedAt(e.getUpdatedAt());
        return a;
    }

    public SecurityAdvisoryEntity toEntity(SecurityAdvisory a) {
        SecurityAdvisoryEntity e = new SecurityAdvisoryEntity();
        e.setId(a.getId());
        e.setOrgId(a.getOrgId());
        e.setCraEventId(a.getCraEventId());
        e.setProductId(a.getProductId());
        e.setTitle(a.getTitle());
        e.setSeverity(a.getSeverity());
        e.setAffectedVersions(a.getAffectedVersions());
        e.setDescription(a.getDescription());
        e.setRemediation(a.getRemediation());
        e.setAdvisoryUrl(a.getAdvisoryUrl());
        e.setStatus(a.getStatus());
        e.setPublishedAt(a.getPublishedAt());
        e.setNotifiedAt(a.getNotifiedAt());
        e.setCreatedBy(a.getCreatedBy());
        e.setCreatedAt(a.getCreatedAt());
        e.setUpdatedAt(a.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // NotificationLog mappings
    // ──────────────────────────────────────────────

    public NotificationLog toDomain(NotificationLogEntity e) {
        NotificationLog n = new NotificationLog();
        n.setId(e.getId());
        n.setOrgId(e.getOrgId());
        n.setCraEventId(e.getCraEventId());
        n.setChannel(e.getChannel());
        n.setRecipient(e.getRecipient());
        n.setSubject(e.getSubject());
        n.setDeadlineType(e.getDeadlineType());
        n.setAlertLevel(e.getAlertLevel());
        n.setSentAt(e.getSentAt());
        return n;
    }

    public NotificationLogEntity toEntity(NotificationLog n) {
        NotificationLogEntity e = new NotificationLogEntity();
        e.setId(n.getId());
        e.setOrgId(n.getOrgId());
        e.setCraEventId(n.getCraEventId());
        e.setChannel(n.getChannel());
        e.setRecipient(n.getRecipient());
        e.setSubject(n.getSubject());
        e.setDeadlineType(n.getDeadlineType());
        e.setAlertLevel(n.getAlertLevel());
        e.setSentAt(n.getSentAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // ProductRepoMapping mappings
    // ──────────────────────────────────────────────

    public ProductRepoMapping toDomain(ProductRepoMappingEntity e) {
        ProductRepoMapping m = new ProductRepoMapping();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setForge(e.getForge());
        m.setProjectId(e.getProjectId());
        m.setRepoUrl(e.getRepoUrl());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    public ProductRepoMappingEntity toEntity(ProductRepoMapping m) {
        ProductRepoMappingEntity e = new ProductRepoMappingEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setForge(m.getForge());
        e.setProjectId(m.getProjectId());
        e.setRepoUrl(m.getRepoUrl());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // VulnerabilityReport mappings
    // ──────────────────────────────────────────────

    public VulnerabilityReport toDomain(VulnerabilityReportEntity e) {
        VulnerabilityReport r = new VulnerabilityReport();
        r.setId(e.getId());
        r.setOrgId(e.getOrgId());
        r.setProductId(e.getProductId());
        r.setTrackingId(e.getTrackingId());
        r.setStatus(VulnerabilityReport.Status.valueOf(e.getStatus()));
        r.setReporterName(e.getReporterName());
        r.setReporterEmail(e.getReporterEmail());
        r.setReporterPgpFingerprint(e.getReporterPgpFingerprint());
        r.setAnonymous(e.isAnonymous());
        r.setTitle(e.getTitle());
        r.setDescription(e.getDescription());
        r.setSeverityEstimate(e.getSeverityEstimate());
        r.setAffectedComponent(e.getAffectedComponent());
        r.setAffectedVersions(e.getAffectedVersions());
        r.setStepsToReproduce(e.getStepsToReproduce());
        r.setProofOfConcept(e.getProofOfConcept());
        r.setAssignedTo(e.getAssignedTo());
        r.setInternalNotes(e.getInternalNotes());
        r.setInternalSeverity(e.getInternalSeverity());
        r.setCvssScore(e.getCvssScore());
        r.setCveId(e.getCveId());
        r.setSubmittedAt(e.getSubmittedAt());
        r.setAcknowledgedAt(e.getAcknowledgedAt());
        r.setTriagedAt(e.getTriagedAt());
        r.setFixedAt(e.getFixedAt());
        r.setDisclosedAt(e.getDisclosedAt());
        r.setDisclosureDeadline(e.getDisclosureDeadline());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    public VulnerabilityReportEntity toEntity(VulnerabilityReport r) {
        VulnerabilityReportEntity e = new VulnerabilityReportEntity();
        e.setId(r.getId());
        e.setOrgId(r.getOrgId());
        e.setProductId(r.getProductId());
        e.setTrackingId(r.getTrackingId());
        e.setStatus(r.getStatus().name());
        e.setReporterName(r.getReporterName());
        e.setReporterEmail(r.getReporterEmail());
        e.setReporterPgpFingerprint(r.getReporterPgpFingerprint());
        e.setAnonymous(r.isAnonymous());
        e.setTitle(r.getTitle());
        e.setDescription(r.getDescription());
        e.setSeverityEstimate(r.getSeverityEstimate());
        e.setAffectedComponent(r.getAffectedComponent());
        e.setAffectedVersions(r.getAffectedVersions());
        e.setStepsToReproduce(r.getStepsToReproduce());
        e.setProofOfConcept(r.getProofOfConcept());
        e.setAssignedTo(r.getAssignedTo());
        e.setInternalNotes(r.getInternalNotes());
        e.setInternalSeverity(r.getInternalSeverity());
        e.setCvssScore(r.getCvssScore());
        e.setCveId(r.getCveId());
        e.setSubmittedAt(r.getSubmittedAt());
        e.setAcknowledgedAt(r.getAcknowledgedAt());
        e.setTriagedAt(r.getTriagedAt());
        e.setFixedAt(r.getFixedAt());
        e.setDisclosedAt(r.getDisclosedAt());
        e.setDisclosureDeadline(r.getDisclosureDeadline());
        e.setCreatedAt(r.getCreatedAt());
        e.setUpdatedAt(r.getUpdatedAt());
        return e;
    }

    // ──────────────────────────────────────────────
    // JSON serialization helpers
    // ──────────────────────────────────────────────

    private String serializeContacts(List<Map<String, String>> contacts) {
        if (contacts == null) return "[]";
        try {
            return objectMapper.writeValueAsString(contacts);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize contacts", e);
        }
    }

    private List<Map<String, String>> deserializeContacts(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize contacts", e);
        }
    }
}
