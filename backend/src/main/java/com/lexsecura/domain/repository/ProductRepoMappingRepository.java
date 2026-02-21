package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.ProductRepoMapping;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepoMappingRepository {

    ProductRepoMapping save(ProductRepoMapping mapping);

    Optional<ProductRepoMapping> findByForgeAndProjectId(String forge, long projectId);

    Optional<ProductRepoMapping> findById(UUID id);

    void deleteById(UUID id);
}
