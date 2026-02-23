package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.domain.model.AuditEvent;
import com.lexsecura.domain.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    private AuditService auditService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        auditService = new AuditService(auditEventRepository, objectMapper);
    }

    @Test
    void computeHash_shouldBeConsistentForSameInput() {
        UUID entityId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        String hash1 = auditService.computeHash(null, "{\"key\":\"value\"}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);
        String hash2 = auditService.computeHash(null, "{\"key\":\"value\"}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 hex is 64 chars
    }

    @Test
    void computeHash_shouldDifferWithDifferentPrevHash() {
        UUID entityId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        String hash1 = auditService.computeHash(null, "{}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);
        String hash2 = auditService.computeHash("abc123", "{}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void computeHash_shouldDifferWithDifferentPayload() {
        UUID entityId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        String hash1 = auditService.computeHash(null, "{\"a\":1}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);
        String hash2 = auditService.computeHash(null, "{\"b\":2}", "PRODUCT",
                entityId, "CREATE", actor, createdAt);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void record_shouldSaveEventWithHash() {
        when(auditEventRepository.findTopByOrgIdOrderByCreatedAtDesc(orgId)).thenReturn(Optional.empty());
        when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.record(orgId, "PRODUCT", UUID.randomUUID(), "CREATE", actor,
                Map.of("name", "Test"));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();

        assertNotNull(saved.getHash());
        assertEquals(64, saved.getHash().length());
        assertNull(saved.getPrevHash()); // first event
        assertEquals(orgId, saved.getOrgId());
        assertEquals("PRODUCT", saved.getEntityType());
        assertEquals("CREATE", saved.getAction());
    }

    @Test
    void record_shouldChainHash() {
        String previousHash = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        AuditEvent previousEvent = new AuditEvent();
        previousEvent.setHash(previousHash);

        when(auditEventRepository.findTopByOrgIdOrderByCreatedAtDesc(orgId))
                .thenReturn(Optional.of(previousEvent));
        when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.record(orgId, "RELEASE", UUID.randomUUID(), "CREATE", actor,
                Map.of("version", "1.0"));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();

        assertEquals(previousHash, saved.getPrevHash());
        assertNotNull(saved.getHash());
        assertNotEquals(previousHash, saved.getHash());
    }

    @Test
    void verify_emptyChain_shouldReturnValid() {
        when(auditEventRepository.countByOrgId(orgId)).thenReturn(0L);

        AuditVerifyResponse result = auditService.verify(orgId);

        assertTrue(result.valid());
        assertEquals(0, result.totalEvents());
    }

    @Test
    void verify_validChain_shouldReturnValid() {
        UUID entityId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-01T01:00:00Z");

        // Build a valid chain
        String payload1 = "{\"name\":\"Product A\"}";
        String hash1 = auditService.computeHash(null, payload1, "PRODUCT", entityId, "CREATE", actor, t1);

        AuditEvent event1 = new AuditEvent();
        event1.setId(UUID.randomUUID());
        event1.setOrgId(orgId);
        event1.setEntityType("PRODUCT");
        event1.setEntityId(entityId);
        event1.setAction("CREATE");
        event1.setActor(actor);
        event1.setPayloadJson(payload1);
        event1.setCreatedAt(t1);
        event1.setPrevHash(null);
        event1.setHash(hash1);

        String payload2 = "{\"name\":\"Product A Updated\"}";
        String hash2 = auditService.computeHash(hash1, payload2, "PRODUCT", entityId, "UPDATE", actor, t2);

        AuditEvent event2 = new AuditEvent();
        event2.setId(UUID.randomUUID());
        event2.setOrgId(orgId);
        event2.setEntityType("PRODUCT");
        event2.setEntityId(entityId);
        event2.setAction("UPDATE");
        event2.setActor(actor);
        event2.setPayloadJson(payload2);
        event2.setCreatedAt(t2);
        event2.setPrevHash(hash1);
        event2.setHash(hash2);

        when(auditEventRepository.countByOrgId(orgId)).thenReturn(2L);
        when(auditEventRepository.findByOrgIdOrderByCreatedAtAsc(orgId, 0, 500))
                .thenReturn(List.of(event1, event2));

        AuditVerifyResponse result = auditService.verify(orgId);

        assertTrue(result.valid());
        assertEquals(2, result.totalEvents());
        assertEquals(2, result.verifiedEvents());
    }

    @Test
    void verify_tamperedPayload_shouldDetect() {
        UUID entityId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");

        String payload = "{\"name\":\"Product A\"}";
        String hash = auditService.computeHash(null, payload, "PRODUCT", entityId, "CREATE", actor, t1);

        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(orgId);
        event.setEntityType("PRODUCT");
        event.setEntityId(entityId);
        event.setAction("CREATE");
        event.setActor(actor);
        event.setPayloadJson("{\"name\":\"TAMPERED\"}"); // tampered!
        event.setCreatedAt(t1);
        event.setPrevHash(null);
        event.setHash(hash);

        when(auditEventRepository.countByOrgId(orgId)).thenReturn(1L);
        when(auditEventRepository.findByOrgIdOrderByCreatedAtAsc(orgId, 0, 500))
                .thenReturn(List.of(event));

        AuditVerifyResponse result = auditService.verify(orgId);

        assertFalse(result.valid());
        assertTrue(result.message().contains("Hash mismatch"));
    }

    @Test
    void verify_brokenChain_shouldDetect() {
        UUID entityId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-01T01:00:00Z");

        String payload1 = "{\"a\":1}";
        String hash1 = auditService.computeHash(null, payload1, "PRODUCT", entityId, "CREATE", actor, t1);

        AuditEvent event1 = new AuditEvent();
        event1.setId(UUID.randomUUID());
        event1.setOrgId(orgId);
        event1.setEntityType("PRODUCT");
        event1.setEntityId(entityId);
        event1.setAction("CREATE");
        event1.setActor(actor);
        event1.setPayloadJson(payload1);
        event1.setCreatedAt(t1);
        event1.setPrevHash(null);
        event1.setHash(hash1);

        // Second event with wrong prev_hash (not chained to first)
        String payload2 = "{\"b\":2}";
        String wrongPrevHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String hash2 = auditService.computeHash(wrongPrevHash, payload2, "PRODUCT", entityId, "UPDATE", actor, t2);

        AuditEvent event2 = new AuditEvent();
        event2.setId(UUID.randomUUID());
        event2.setOrgId(orgId);
        event2.setEntityType("PRODUCT");
        event2.setEntityId(entityId);
        event2.setAction("UPDATE");
        event2.setActor(actor);
        event2.setPayloadJson(payload2);
        event2.setCreatedAt(t2);
        event2.setPrevHash(wrongPrevHash);
        event2.setHash(hash2);

        when(auditEventRepository.countByOrgId(orgId)).thenReturn(2L);
        when(auditEventRepository.findByOrgIdOrderByCreatedAtAsc(orgId, 0, 500))
                .thenReturn(List.of(event1, event2));

        AuditVerifyResponse result = auditService.verify(orgId);

        assertFalse(result.valid());
        assertTrue(result.message().contains("Chain broken"));
    }
}
