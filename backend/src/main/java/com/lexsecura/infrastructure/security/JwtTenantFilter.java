package com.lexsecura.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTenantFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Always set request_id for correlation
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put("request_id", requestId);
            response.setHeader("X-Request-ID", requestId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                String orgIdClaim = jwt.getClaimAsString("org_id");
                if (orgIdClaim != null) {
                    UUID orgId = UUID.fromString(orgIdClaim);
                    TenantContext.setOrgId(orgId);
                    MDC.put("org_id", orgId.toString());
                }

                String subject = jwt.getSubject();
                if (subject != null) {
                    UUID userId = UUID.fromString(subject);
                    TenantContext.setUserId(userId);
                    MDC.put("user_id", userId.toString());
                }

                log.debug("Tenant context set: org_id={}, user_id={}", orgIdClaim, subject);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }
}
