package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ProductRepoMapping;
import com.lexsecura.domain.repository.ProductRepoMappingRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaProductRepoMappingRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepoMappingRepositoryAdapter implements ProductRepoMappingRepository {

    private final JpaProductRepoMappingRepository jpa;
    private final PersistenceMapper mapper;

    public ProductRepoMappingRepositoryAdapter(JpaProductRepoMappingRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ProductRepoMapping save(ProductRepoMapping mapping) {
        var entity = mapper.toEntity(mapping);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<ProductRepoMapping> findByForgeAndProjectId(String forge, long projectId) {
        return jpa.findByForgeAndProjectId(forge, projectId).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductRepoMapping> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
