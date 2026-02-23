package com.lexsecura.api.controller;

import com.lexsecura.application.dto.WebhookCreateRequest;
import com.lexsecura.application.dto.WebhookResponse;
import com.lexsecura.application.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@PreAuthorize("hasRole('ADMIN')")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookResponse create(@Valid @RequestBody WebhookCreateRequest request) {
        return webhookService.create(request);
    }

    @GetMapping
    public List<WebhookResponse> list() {
        return webhookService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        webhookService.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public void toggle(@PathVariable UUID id, @RequestParam boolean enabled) {
        webhookService.toggleEnabled(id, enabled);
    }
}
