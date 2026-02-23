package com.lexsecura.application.service;

import com.lexsecura.application.dto.ShareLinkCreateRequest;
import com.lexsecura.application.dto.ShareLinkResponse;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.EvidenceType;
import com.lexsecura.domain.model.SbomShareLink;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.domain.repository.SbomShareLinkRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SbomShareServiceTest {

    @Mock private SbomShareLinkRepository shareLinkRepository;
    @Mock private EvidenceRepository evidenceRepository;

    private SbomShareService service;
    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SbomShareService(shareLinkRepository, evidenceRepository);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createShareLink_generatesTokenAndSaves() {
        UUID releaseId = UUID.randomUUID();
        Evidence evidence = new Evidence();
        evidence.setId(UUID.randomUUID());
        evidence.setType(EvidenceType.SBOM);

        when(evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId))
                .thenReturn(List.of(evidence));
        when(shareLinkRepository.save(any())).thenAnswer(i -> {
            SbomShareLink link = i.getArgument(0);
            link.setId(UUID.randomUUID());
            return link;
        });

        ShareLinkResponse result = service.createShareLink(releaseId,
                new ShareLinkCreateRequest("partner@example.com", "Partner Inc", 48, 5, true, false));

        assertNotNull(result.token());
        assertEquals(64, result.token().length()); // 32 bytes hex = 64 chars
        assertEquals("partner@example.com", result.recipientEmail());
        assertEquals(5, result.maxDownloads());
        assertTrue(result.includeVex());
    }

    @Test
    void createShareLink_throwsWhenNoSbom() {
        UUID releaseId = UUID.randomUUID();
        when(evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class, () ->
                service.createShareLink(releaseId,
                        new ShareLinkCreateRequest(null, null, 24, 0, false, false)));
    }

    @Test
    void validateAndConsume_incrementsDownloadCount() {
        SbomShareLink link = new SbomShareLink();
        link.setId(UUID.randomUUID());
        link.setToken("abc123");
        link.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        link.setDownloadCount(0);
        link.setMaxDownloads(10);
        link.setRevoked(false);

        when(shareLinkRepository.findByToken("abc123")).thenReturn(Optional.of(link));
        when(shareLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SbomShareLink result = service.validateAndConsume("abc123");

        assertEquals(1, result.getDownloadCount());
    }

    @Test
    void validateAndConsume_throwsWhenExpired() {
        SbomShareLink link = new SbomShareLink();
        link.setToken("expired");
        link.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        link.setRevoked(false);
        link.setMaxDownloads(0);
        link.setDownloadCount(0);

        when(shareLinkRepository.findByToken("expired")).thenReturn(Optional.of(link));

        assertThrows(IllegalStateException.class, () -> service.validateAndConsume("expired"));
    }

    @Test
    void validateAndConsume_throwsWhenRevoked() {
        SbomShareLink link = new SbomShareLink();
        link.setToken("revoked");
        link.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        link.setRevoked(true);
        link.setMaxDownloads(0);
        link.setDownloadCount(0);

        when(shareLinkRepository.findByToken("revoked")).thenReturn(Optional.of(link));

        assertThrows(IllegalStateException.class, () -> service.validateAndConsume("revoked"));
    }

    @Test
    void revoke_marksSaved() {
        SbomShareLink link = new SbomShareLink();
        link.setId(UUID.randomUUID());
        link.setRevoked(false);

        when(shareLinkRepository.findByIdAndOrgId(link.getId(), orgId)).thenReturn(Optional.of(link));
        when(shareLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.revoke(link.getId());

        ArgumentCaptor<SbomShareLink> captor = ArgumentCaptor.forClass(SbomShareLink.class);
        verify(shareLinkRepository).save(captor.capture());
        assertTrue(captor.getValue().isRevoked());
        assertNotNull(captor.getValue().getRevokedAt());
    }
}
