package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Organization;
import com.lexsecura.domain.repository.OrganizationRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaOrganizationRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrganizationRepositoryAdapter implements OrganizationRepository {

    private final JpaOrganizationRepository jpa;
    private final PersistenceMapper mapper;

    public OrganizationRepositoryAdapter(JpaOrganizationRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Organization save(Organization org) {
        var entity = mapper.toEntity(org);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public List<Organization> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
