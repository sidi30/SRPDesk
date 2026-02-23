package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.ApiKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository {

    ApiKey save(ApiKey apiKey);

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByOrgId(UUID orgId);

    Optional<ApiKey> findByIdAndOrgId(UUID id, UUID orgId);
}
