package com.lexsecura.infrastructure.security;

import com.lexsecura.application.service.ApiKeyService;
import com.lexsecura.domain.model.ApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/integrations/ci/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("request_id", requestId);
        response.setHeader("X-Request-ID", requestId);

        String apiKeyHeader = request.getHeader(API_KEY_HEADER);
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            sendUnauthorized(response, "Missing X-API-Key header");
            return;
        }

        try {
            var maybeKey = apiKeyService.authenticate(apiKeyHeader);
            if (maybeKey.isEmpty()) {
                sendUnauthorized(response, "Invalid or revoked API key");
                return;
            }

            ApiKey apiKey = maybeKey.get();
            TenantContext.setOrgId(apiKey.getOrgId());
            TenantContext.setUserId(apiKey.getCreatedBy());
            MDC.put("org_id", apiKey.getOrgId().toString());
            MDC.put("user_id", apiKey.getCreatedBy().toString());
            MDC.put("api_key_id", apiKey.getId().toString());

            log.debug("API key authenticated: keyId={}, org={}", apiKey.getId(), apiKey.getOrgId());

            apiKeyService.updateLastUsed(apiKey);

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"https://lexsecura.com/problems/unauthorized",\
                "title":"Unauthorized","status":401,\
                "detail":"%s"}""".formatted(detail));
    }
}
