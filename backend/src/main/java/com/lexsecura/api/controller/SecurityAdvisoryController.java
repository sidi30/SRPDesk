package com.lexsecura.api.controller;

import com.lexsecura.application.dto.SecurityAdvisoryCreateRequest;
import com.lexsecura.application.dto.SecurityAdvisoryResponse;
import com.lexsecura.application.service.SecurityAdvisoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/security-advisories")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
public class SecurityAdvisoryController {

    private final SecurityAdvisoryService advisoryService;

    public SecurityAdvisoryController(SecurityAdvisoryService advisoryService) {
        this.advisoryService = advisoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityAdvisoryResponse create(@Valid @RequestBody SecurityAdvisoryCreateRequest request) {
        return advisoryService.create(request);
    }

    @GetMapping
    public List<SecurityAdvisoryResponse> list() {
        return advisoryService.list();
    }

    @GetMapping("/{id}")
    public SecurityAdvisoryResponse findById(@PathVariable UUID id) {
        return advisoryService.findById(id);
    }

    @PostMapping("/{id}/publish")
    public SecurityAdvisoryResponse publish(@PathVariable UUID id) {
        return advisoryService.publish(id);
    }

    @PostMapping("/{id}/notify")
    public SecurityAdvisoryResponse notify(@PathVariable UUID id, @RequestBody List<String> recipients) {
        return advisoryService.notifyUsers(id, recipients);
    }
}
