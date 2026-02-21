package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.OrgMember;
import com.lexsecura.domain.repository.OrgMemberRepository;
import com.lexsecura.infrastructure.persistence.entity.OrgMemberEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaOrgMemberRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OrgMemberRepositoryAdapter implements OrgMemberRepository {

    private final JpaOrgMemberRepository jpa;

    public OrgMemberRepositoryAdapter(JpaOrgMemberRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrgMember save(OrgMember member) {
        OrgMemberEntity entity = toEntity(member);
        entity = jpa.save(entity);
        return toDomain(entity);
    }

    @Override
    public List<OrgMember> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrgMember> findByOrgIdAndUserId(UUID orgId, UUID userId) {
        return jpa.findByOrgIdAndUserId(orgId, userId).map(this::toDomain);
    }

    @Override
    public List<OrgMember> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private OrgMember toDomain(OrgMemberEntity entity) {
        OrgMember member = new OrgMember();
        member.setId(entity.getId());
        member.setOrgId(entity.getOrgId());
        member.setUserId(entity.getUserId());
        member.setEmail(entity.getEmail());
        member.setRole(entity.getRole());
        member.setJoinedAt(entity.getJoinedAt());
        return member;
    }

    private OrgMemberEntity toEntity(OrgMember domain) {
        OrgMemberEntity entity = new OrgMemberEntity();
        entity.setId(domain.getId());
        entity.setOrgId(domain.getOrgId());
        entity.setUserId(domain.getUserId());
        entity.setEmail(domain.getEmail());
        entity.setRole(domain.getRole());
        entity.setJoinedAt(domain.getJoinedAt());
        return entity;
    }
}
