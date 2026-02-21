package com.lexsecura.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lexsecura.application.dto.AuditEventResponse;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.domain.model.AuditEvent;
import com.lexsecura.domain.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper canonicalMapper;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        // Create a canonical mapper with sorted keys for deterministic hashing
        this.canonicalMapper = objectMapper.copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public void record(UUID orgId, String entityType, UUID entityId,
                       String action, UUID actor, Object payload) {
        try {
            String payloadJson = canonicalMapper.writeValueAsString(payload);
            // Truncate to microseconds to match PostgreSQL TIMESTAMPTZ precision
            Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

            // Get the last event's hash for this org
            String prevHash = auditEventRepository.findTopByOrgIdOrderByCreatedAtDesc(orgId)
                    .map(AuditEvent::getHash)
                    .orElse(null);

            String hash = computeHash(prevHash, payloadJson, entityType, entityId, action, actor, createdAt);

            AuditEvent event = new AuditEvent();
            event.setOrgId(orgId);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setAction(action);
            event.setActor(actor);
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(createdAt);
            event.setPrevHash(prevHash);
            event.setHash(hash);

            auditEventRepository.save(event);
            log.debug("Audit event recorded: entity={}/{}, action={}", entityType, entityId, action);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit payload", e);
            throw new RuntimeException("Failed to serialize audit payload", e);
        }
    }

    @Transactional(readOnly = true)
    public AuditVerifyResponse verify(UUID orgId) {
        List<AuditEvent> events = auditEventRepository.findAllByOrgIdOrderByCreatedAt(orgId);

        if (events.isEmpty()) {
            return new AuditVerifyResponse(true, 0, 0, "No audit events found");
        }

        int verified = 0;
        for (int i = 0; i < events.size(); i++) {
            AuditEvent event = events.get(i);
            String expectedPrevHash = (i == 0) ? null : events.get(i - 1).getHash();

            // Verify prev_hash chain linkage
            if (i == 0) {
                if (event.getPrevHash() != null) {
                    return new AuditVerifyResponse(false, events.size(), verified,
                            "First event has non-null prev_hash at event " + event.getId());
                }
            } else {
                if (!expectedPrevHash.equals(event.getPrevHash())) {
                    return new AuditVerifyResponse(false, events.size(), verified,
                            "Chain broken at event " + event.getId() + ": expected prev_hash="
                                    + expectedPrevHash + " but found=" + event.getPrevHash());
                }
            }

            // Recompute and verify hash
            String recomputedHash = computeHash(
                    event.getPrevHash(), event.getPayloadJson(),
                    event.getEntityType(), event.getEntityId(),
                    event.getAction(), event.getActor(), event.getCreatedAt());

            if (!recomputedHash.equals(event.getHash())) {
                return new AuditVerifyResponse(false, events.size(), verified,
                        "Hash mismatch at event " + event.getId() + ": expected="
                                + recomputedHash + " but found=" + event.getHash());
            }

            verified++;
        }

        return new AuditVerifyResponse(true, events.size(), verified, "All events verified successfully");
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> findEvents(String entityType, UUID entityId) {
        return auditEventRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAt(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> findAllByOrgId(UUID orgId) {
        return auditEventRepository.findAllByOrgIdOrderByCreatedAt(orgId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public String computeHash(String prevHash, String payloadJson,
                                String entityType, UUID entityId,
                                String action, UUID actor, Instant createdAt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            if (prevHash != null) {
                sb.append(prevHash);
            }
            sb.append(payloadJson);
            sb.append(entityType);
            sb.append(entityId.toString());
            sb.append(action);
            sb.append(actor.toString());
            sb.append(createdAt.toString());

            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private AuditEventResponse toResponse(AuditEvent e) {
        return new AuditEventResponse(
                e.getId(), e.getOrgId(), e.getEntityType(), e.getEntityId(),
                e.getAction(), e.getActor(), e.getPayloadJson(),
                e.getCreatedAt(), e.getPrevHash(), e.getHash());
    }
}
