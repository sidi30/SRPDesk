package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findByIdAndOrgId(UUID id, UUID orgId);

    List<Product> findAllByOrgId(UUID orgId);

    Optional<Product> findByNameAndOrgId(String name, UUID orgId);

    void deleteByIdAndOrgId(UUID id, UUID orgId);
}
