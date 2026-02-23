package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaProductRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpa;
    private final PersistenceMapper mapper;

    public ProductRepositoryAdapter(JpaProductRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        var entity = mapper.toEntity(product);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Product> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findByNameAndOrgId(String name, UUID orgId) {
        return jpa.findByNameAndOrgId(name, orgId).map(mapper::toDomain);
    }

    @Override
    public void deleteByIdAndOrgId(UUID id, UUID orgId) {
        jpa.deleteByIdAndOrgId(id, orgId);
    }
}
