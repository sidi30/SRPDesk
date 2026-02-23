package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.SecurityAdvisoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSecurityAdvisoryRepository extends JpaRepository<SecurityAdvisoryEntity, UUID> {

    Optional<SecurityAdvisoryEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<SecurityAdvisoryEntity> findAllByOrgId(UUID orgId);

    List<SecurityAdvisoryEntity> findAllByCraEventId(UUID craEventId);
}
