package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Release;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReleaseRepository {

    Release save(Release release);

    Optional<Release> findById(UUID id);

    Optional<Release> findByIdAndOrgId(UUID id, UUID orgId);

    List<Release> findAllByProductId(UUID productId);

    void deleteById(UUID id);
}
