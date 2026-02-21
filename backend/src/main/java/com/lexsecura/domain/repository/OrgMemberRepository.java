package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.OrgMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgMemberRepository {
    OrgMember save(OrgMember member);
    List<OrgMember> findAllByOrgId(UUID orgId);
    Optional<OrgMember> findByOrgIdAndUserId(UUID orgId, UUID userId);
    List<OrgMember> findAllByUserId(UUID userId);
}
