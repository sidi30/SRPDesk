package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.OrgMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrgMemberRepository extends JpaRepository<OrgMemberEntity, UUID> {

    List<OrgMemberEntity> findAllByOrgId(UUID orgId);

    Optional<OrgMemberEntity> findByOrgIdAndUserId(UUID orgId, UUID userId);

    List<OrgMemberEntity> findAllByUserId(UUID userId);
}
