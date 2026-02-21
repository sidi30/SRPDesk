package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ProductRepoMapping;
import com.lexsecura.domain.repository.ProductRepoMappingRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaProductRepoMappingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepoMappingRepositoryAdapter implements ProductRepoMappingRepository {

    private final JpaProductRepoMappingRepository jpa;

    public ProductRepoMappingRepositoryAdapter(JpaProductRepoMappingRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ProductRepoMapping save(ProductRepoMapping mapping) {
        var entity = toEntity(mapping);
        entity = jpa.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<ProductRepoMapping> findByForgeAndProjectId(String forge, long projectId) {
        return jpa.findByForgeAndProjectId(forge, projectId).map(this::toDomain);
    }

    @Override
    public Optional<ProductRepoMapping> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private ProductRepoMapping toDomain(com.lexsecura.infrastructure.persistence.entity.ProductRepoMappingEntity e) {
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

    private com.lexsecura.infrastructure.persistence.entity.ProductRepoMappingEntity toEntity(ProductRepoMapping m) {
        var e = new com.lexsecura.infrastructure.persistence.entity.ProductRepoMappingEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setForge(m.getForge());
        e.setProjectId(m.getProjectId());
        e.setRepoUrl(m.getRepoUrl());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
