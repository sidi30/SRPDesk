package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {
    Organization save(Organization org);
    Optional<Organization> findById(UUID id);
    Optional<Organization> findBySlug(String slug);
    List<Organization> findAllByUserId(UUID userId);
}
