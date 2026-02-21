package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Organization;
import com.lexsecura.domain.repository.OrganizationRepository;
import com.lexsecura.infrastructure.persistence.entity.OrganizationEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaOrganizationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OrganizationRepositoryAdapter implements OrganizationRepository {

    private final JpaOrganizationRepository jpa;

    public OrganizationRepositoryAdapter(JpaOrganizationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Organization save(Organization org) {
        OrganizationEntity entity = toEntity(org);
        entity = jpa.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<Organization> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Organization toDomain(OrganizationEntity entity) {
        Organization org = new Organization();
        org.setId(entity.getId());
        org.setName(entity.getName());
        org.setSlug(entity.getSlug());
        org.setCreatedAt(entity.getCreatedAt());
        org.setUpdatedAt(entity.getUpdatedAt());
        return org;
    }

    private OrganizationEntity toEntity(Organization domain) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
