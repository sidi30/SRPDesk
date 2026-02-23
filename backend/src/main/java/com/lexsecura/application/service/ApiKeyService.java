package com.lexsecura.application.service;

import com.lexsecura.application.dto.ApiKeyCreateResponse;
import com.lexsecura.application.dto.ApiKeyResponse;
import com.lexsecura.domain.model.ApiKey;
import com.lexsecura.domain.repository.ApiKeyRepository;
import com.lexsecura.infrastructure.config.ApiKeyProperties;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyProperties properties;
    private final SecureRandom secureRandom;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyProperties properties) {
        this.apiKeyRepository = apiKeyRepository;
        this.properties = properties;
        this.secureRandom = new SecureRandom();
    }

    public ApiKeyCreateResponse create(String name) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        byte[] randomBytes = new byte[properties.getRandomBytes()];
        secureRandom.nextBytes(randomBytes);
        String randomHex = HexFormat.of().formatHex(randomBytes);
        String plainTextKey = properties.getPrefix() + randomHex;
        String keyPrefix = plainTextKey.substring(0, Math.min(12, plainTextKey.length()));
        String keyHash = sha256(plainTextKey);

        ApiKey apiKey = new ApiKey(orgId, name, keyPrefix, keyHash, userId);
        apiKey.setScopes(properties.getDefaultScopes());
        apiKey = apiKeyRepository.save(apiKey);

        log.info("API key created: id={}, prefix={}, org={}", apiKey.getId(), keyPrefix, orgId);

        return new ApiKeyCreateResponse(apiKey.getId(), apiKey.getName(), keyPrefix, plainTextKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> list() {
        UUID orgId = TenantContext.getOrgId();
        return apiKeyRepository.findAllByOrgId(orgId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void revoke(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        ApiKey apiKey = apiKeyRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("API key not found: " + id));

        if (apiKey.isRevoked()) {
            throw new IllegalStateException("API key already revoked");
        }

        apiKey.setRevoked(true);
        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);

        log.info("API key revoked: id={}, org={}", id, orgId);
    }

    public Optional<ApiKey> authenticate(String rawKey) {
        if (rawKey == null || !rawKey.startsWith(properties.getPrefix())) {
            return Optional.empty();
        }
        String keyHash = sha256(rawKey);
        return apiKeyRepository.findByKeyHash(keyHash)
                .filter(k -> !k.isRevoked());
    }

    public void updateLastUsed(ApiKey apiKey) {
        apiKey.setLastUsedAt(Instant.now());
        apiKeyRepository.save(apiKey);
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return new ApiKeyResponse(
                key.getId(), key.getName(), key.getKeyPrefix(),
                key.getScopes(), key.getCreatedAt(), key.getLastUsedAt(),
                key.isRevoked(), key.getRevokedAt()
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
