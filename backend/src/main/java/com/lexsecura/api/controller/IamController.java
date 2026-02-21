package com.lexsecura.api.controller;

import com.lexsecura.application.dto.*;
import com.lexsecura.application.service.IamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "IAM", description = "Identity and Access Management")
public class IamController {

    private final IamService iamService;

    public IamController(IamService iamService) {
        this.iamService = iamService;
    }

    @PostMapping("/orgs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create organization")
    public OrganizationResponse createOrganization(@Valid @RequestBody OrganizationCreateRequest request) {
        return iamService.createOrganization(request);
    }

    @GetMapping("/orgs")
    @Operation(summary = "List organizations for current user")
    public List<OrganizationResponse> listOrganizations() {
        return iamService.listOrganizations();
    }

    @PostMapping("/orgs/{orgId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add member to organization")
    public OrgMemberResponse addMember(@PathVariable UUID orgId,
                                        @Valid @RequestBody OrgMemberCreateRequest request) {
        return iamService.addMember(orgId, request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public MeResponse getMe() {
        return iamService.getMe();
    }
}
