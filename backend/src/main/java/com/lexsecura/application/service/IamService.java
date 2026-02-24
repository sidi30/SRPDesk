package com.lexsecura.application.service;

import com.lexsecura.application.dto.*;
import com.lexsecura.domain.model.OrgMember;
import com.lexsecura.domain.model.Organization;
import com.lexsecura.domain.repository.OrgMemberRepository;
import com.lexsecura.domain.repository.OrganizationRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class IamService {

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final AuditService auditService;

    public IamService(OrganizationRepository organizationRepository,
                      OrgMemberRepository orgMemberRepository,
                      AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.orgMemberRepository = orgMemberRepository;
        this.auditService = auditService;
    }

    public OrganizationResponse createOrganization(OrganizationCreateRequest request) {
        UUID userId = TenantContext.getUserId();

        // Check slug uniqueness
        if (organizationRepository.findBySlug(request.slug()).isPresent()) {
            throw new IllegalArgumentException("Organization slug already exists: " + request.slug());
        }

        Organization org = new Organization();
        org.setName(request.name());
        org.setSlug(request.slug());
        org.setCreatedAt(Instant.now());
        org.setUpdatedAt(Instant.now());
        org = organizationRepository.save(org);

        // Add creator as ADMIN member
        OrgMember member = new OrgMember();
        member.setOrgId(org.getId());
        member.setUserId(userId);
        member.setEmail(""); // Will be filled from JWT in real scenario
        member.setRole("ADMIN");
        member.setJoinedAt(Instant.now());
        orgMemberRepository.save(member);

        auditService.record(org.getId(), "ORGANIZATION", org.getId(), "CREATE", userId,
                Map.of("name", org.getName(), "slug", org.getSlug()));

        return toOrgResponse(org);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> listOrganizations() {
        UUID userId = TenantContext.getUserId();
        return organizationRepository.findAllByUserId(userId).stream()
                .map(this::toOrgResponse)
                .toList();
    }

    public OrgMemberResponse addMember(UUID orgId, OrgMemberCreateRequest request) {
        UUID actorId = TenantContext.getUserId();

        organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + orgId));

        // Check for duplicate
        if (orgMemberRepository.findByOrgIdAndUserId(orgId, request.userId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this organization");
        }

        // Validate role
        Set<String> validRoles = Set.of("ADMIN", "COMPLIANCE_MANAGER", "CONTRIBUTOR");
        if (!validRoles.contains(request.role())) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        OrgMember member = new OrgMember();
        member.setOrgId(orgId);
        member.setUserId(request.userId());
        member.setEmail(request.email());
        member.setRole(request.role());
        member.setJoinedAt(Instant.now());
        member = orgMemberRepository.save(member);

        auditService.record(orgId, "ORG_MEMBER", member.getId(), "ADD", actorId,
                Map.of("userId", request.userId().toString(), "role", request.role()));

        return toMemberResponse(member);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe() {
        UUID userId = TenantContext.getUserId();
        UUID orgId = TenantContext.getOrgId();

        List<OrgMember> memberships = orgMemberRepository.findAllByUserId(userId);
        List<MeResponse.OrgSummary> orgSummaries = new ArrayList<>();

        for (OrgMember m : memberships) {
            Organization org = organizationRepository.findById(m.getOrgId()).orElse(null);
            if (org != null) {
                orgSummaries.add(new MeResponse.OrgSummary(org.getId(), org.getName(), m.getRole()));
            }
        }

        // Find current member's email and roles
        String email = memberships.stream()
                .filter(m -> m.getOrgId().equals(orgId))
                .findFirst()
                .map(OrgMember::getEmail)
                .orElse("");

        List<String> roles = memberships.stream()
                .filter(m -> m.getOrgId().equals(orgId))
                .map(OrgMember::getRole)
                .toList();

        return new MeResponse(userId, email, orgId, roles, orgSummaries);
    }

    private OrganizationResponse toOrgResponse(Organization org) {
        return new OrganizationResponse(org.getId(), org.getName(), org.getSlug(), org.getCreatedAt());
    }

    private OrgMemberResponse toMemberResponse(OrgMember m) {
        return new OrgMemberResponse(m.getId(), m.getUserId(), m.getEmail(), m.getRole(), m.getJoinedAt());
    }
}
