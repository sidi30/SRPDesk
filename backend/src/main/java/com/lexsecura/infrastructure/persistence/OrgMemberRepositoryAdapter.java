package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.OrgMember;
import com.lexsecura.domain.repository.OrgMemberRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaOrgMemberRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrgMemberRepositoryAdapter implements OrgMemberRepository {

    private final JpaOrgMemberRepository jpa;
    private final PersistenceMapper mapper;

    public OrgMemberRepositoryAdapter(JpaOrgMemberRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public OrgMember save(OrgMember member) {
        var entity = mapper.toEntity(member);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public List<OrgMember> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<OrgMember> findByOrgIdAndUserId(UUID orgId, UUID userId) {
        return jpa.findByOrgIdAndUserId(orgId, userId).map(mapper::toDomain);
    }

    @Override
    public List<OrgMember> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
