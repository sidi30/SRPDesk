package com.lexsecura.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.*;
import com.lexsecura.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PersistenceMapper {

    private final ObjectMapper objectMapper;

    public PersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Product mappings
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

    // Release mappings
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
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        return e;
    }

    // Evidence mappings
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

    // ApiKey mappings
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

    // AuditEvent mappings
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
