package com.lexsecura.application.service;

import com.lexsecura.application.dto.ApiKeyCreateResponse;
import com.lexsecura.application.dto.ApiKeyResponse;
import com.lexsecura.domain.model.ApiKey;
import com.lexsecura.domain.repository.ApiKeyRepository;
import com.lexsecura.infrastructure.config.ApiKeyProperties;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private ApiKeyService apiKeyService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        ApiKeyProperties properties = new ApiKeyProperties();
        apiKeyService = new ApiKeyService(apiKeyRepository, properties);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldGenerateKeyWithPrefix() {
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId(UUID.randomUUID());
            return k;
        });

        ApiKeyCreateResponse response = apiKeyService.create("Test Key");

        assertNotNull(response);
        assertEquals("Test Key", response.name());
        assertTrue(response.plainTextKey().startsWith("srpd_"));
        assertEquals(45, response.plainTextKey().length()); // "srpd_" + 40 hex
        assertEquals(response.plainTextKey().substring(0, 12), response.keyPrefix());

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey saved = captor.getValue();
        assertEquals(orgId, saved.getOrgId());
        assertEquals(userId, saved.getCreatedBy());
        assertFalse(saved.isRevoked());
    }

    @Test
    void create_shouldStoreHashNotPlaintext() {
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId(UUID.randomUUID());
            return k;
        });

        ApiKeyCreateResponse response = apiKeyService.create("Key");

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        String storedHash = captor.getValue().getKeyHash();

        // Verify stored hash matches SHA-256 of plaintext
        String expectedHash = sha256(response.plainTextKey());
        assertEquals(expectedHash, storedHash);
    }

    @Test
    void list_shouldReturnKeysForCurrentOrg() {
        ApiKey key1 = createApiKey("Key 1");
        ApiKey key2 = createApiKey("Key 2");
        when(apiKeyRepository.findAllByOrgId(orgId)).thenReturn(List.of(key1, key2));

        List<ApiKeyResponse> result = apiKeyService.list();

        assertEquals(2, result.size());
        assertEquals("Key 1", result.get(0).name());
        assertEquals("Key 2", result.get(1).name());
    }

    @Test
    void revoke_shouldMarkKeyAsRevoked() {
        UUID keyId = UUID.randomUUID();
        ApiKey key = createApiKey("Test");
        key.setId(keyId);
        when(apiKeyRepository.findByIdAndOrgId(keyId, orgId)).thenReturn(Optional.of(key));
        when(apiKeyRepository.save(any())).thenReturn(key);

        apiKeyService.revoke(keyId);

        assertTrue(key.isRevoked());
        assertNotNull(key.getRevokedAt());
        verify(apiKeyRepository).save(key);
    }

    @Test
    void revoke_alreadyRevoked_shouldThrow() {
        UUID keyId = UUID.randomUUID();
        ApiKey key = createApiKey("Test");
        key.setId(keyId);
        key.setRevoked(true);
        when(apiKeyRepository.findByIdAndOrgId(keyId, orgId)).thenReturn(Optional.of(key));

        assertThrows(IllegalStateException.class, () -> apiKeyService.revoke(keyId));
    }

    @Test
    void revoke_notFound_shouldThrow() {
        UUID keyId = UUID.randomUUID();
        when(apiKeyRepository.findByIdAndOrgId(keyId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> apiKeyService.revoke(keyId));
    }

    @Test
    void authenticate_validKey_shouldReturnApiKey() {
        String plainKey = "srpd_abcdef1234567890abcdef1234567890abcd";
        String hash = sha256(plainKey);
        ApiKey key = createApiKey("Test");
        key.setKeyHash(hash);
        when(apiKeyRepository.findByKeyHash(hash)).thenReturn(Optional.of(key));

        Optional<ApiKey> result = apiKeyService.authenticate(plainKey);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void authenticate_revokedKey_shouldReturnEmpty() {
        String plainKey = "srpd_abcdef1234567890abcdef1234567890abcd";
        String hash = sha256(plainKey);
        ApiKey key = createApiKey("Test");
        key.setKeyHash(hash);
        key.setRevoked(true);
        when(apiKeyRepository.findByKeyHash(hash)).thenReturn(Optional.of(key));

        Optional<ApiKey> result = apiKeyService.authenticate(plainKey);

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_invalidKey_shouldReturnEmpty() {
        Optional<ApiKey> result = apiKeyService.authenticate("invalid_key");
        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_nullKey_shouldReturnEmpty() {
        Optional<ApiKey> result = apiKeyService.authenticate(null);
        assertTrue(result.isEmpty());
    }

    private ApiKey createApiKey(String name) {
        ApiKey key = new ApiKey(orgId, name, "srpd_abc1234", sha256("test"), userId);
        key.setId(UUID.randomUUID());
        return key;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
