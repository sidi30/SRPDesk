package com.lexsecura.api.controller;

import com.lexsecura.application.dto.CiPolicyRequest;
import com.lexsecura.application.dto.CiPolicyResponse;
import com.lexsecura.domain.model.CiPolicy;
import com.lexsecura.domain.repository.CiPolicyRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ci-policy")
public class CiPolicyController {

    private final CiPolicyRepository ciPolicyRepository;

    public CiPolicyController(CiPolicyRepository ciPolicyRepository) {
        this.ciPolicyRepository = ciPolicyRepository;
    }

    @GetMapping
    public ResponseEntity<CiPolicyResponse> getPolicy() {
        UUID orgId = TenantContext.getOrgId();
        return ciPolicyRepository.findByOrgId(orgId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CiPolicyResponse> upsertPolicy(@RequestBody CiPolicyRequest request) {
        UUID orgId = TenantContext.getOrgId();

        CiPolicy policy = ciPolicyRepository.findByOrgId(orgId).orElseGet(() -> {
            CiPolicy p = new CiPolicy();
            p.setOrgId(orgId);
            return p;
        });

        policy.setMaxCritical(request.maxCritical());
        policy.setMaxHigh(request.maxHigh());
        policy.setMinQualityScore(request.minQualityScore());
        policy.setBlockOnFail(request.blockOnFail());

        policy = ciPolicyRepository.save(policy);
        return ResponseEntity.ok(toResponse(policy));
    }

    private CiPolicyResponse toResponse(CiPolicy p) {
        return new CiPolicyResponse(
                p.getId(), p.getMaxCritical(), p.getMaxHigh(),
                p.getMinQualityScore(), p.isBlockOnFail(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
