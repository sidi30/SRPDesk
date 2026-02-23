package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.SecurityAdvisory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecurityAdvisoryRepository {

    SecurityAdvisory save(SecurityAdvisory advisory);

    Optional<SecurityAdvisory> findByIdAndOrgId(UUID id, UUID orgId);

    List<SecurityAdvisory> findAllByOrgId(UUID orgId);

    List<SecurityAdvisory> findAllByCraEventId(UUID craEventId);
}
