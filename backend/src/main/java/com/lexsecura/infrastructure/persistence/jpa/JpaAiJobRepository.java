package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.AiJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAiJobRepository extends JpaRepository<AiJobEntity, UUID> {

    Optional<AiJobEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<AiJobEntity> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId);
}
