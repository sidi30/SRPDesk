package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ApiKey;
import com.lexsecura.domain.repository.ApiKeyRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaApiKeyRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final JpaApiKeyRepository jpa;
    private final PersistenceMapper mapper;

    public ApiKeyRepositoryAdapter(JpaApiKeyRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        var entity = mapper.toEntity(apiKey);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpa.findByKeyHash(keyHash).map(mapper::toDomain);
    }

    @Override
    public List<ApiKey> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ApiKey> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }
}
