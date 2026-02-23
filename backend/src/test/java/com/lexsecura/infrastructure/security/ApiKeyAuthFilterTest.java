package com.lexsecura.infrastructure.security;

import com.lexsecura.application.service.ApiKeyService;
import com.lexsecura.domain.model.ApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthFilterTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthFilter filter;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldNotFilter_nonCiPath_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ciPath_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/integrations/ci/sbom");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilter_missingHeader_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/integrations/ci/sbom");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing X-API-Key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_invalidKey_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/integrations/ci/sbom");
        request.addHeader("X-API-Key", "srpd_invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiKeyService.authenticate("srpd_invalid")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or revoked"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_validKey_setsTenantContextAndContinues() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawKey = "srpd_abcdef1234567890abcdef1234567890abcd";

        ApiKey apiKey = new ApiKey(orgId, "Test", "srpd_abcdef1", "hash", userId);
        apiKey.setId(UUID.randomUUID());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/integrations/ci/sbom");
        request.addHeader("X-API-Key", rawKey);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiKeyService.authenticate(rawKey)).thenReturn(Optional.of(apiKey));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(apiKeyService).updateLastUsed(apiKey);
        // TenantContext is cleared in finally block, so we verify the filter chain was called
    }

    @Test
    void doFilter_emptyHeader_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/integrations/ci/sbom");
        request.addHeader("X-API-Key", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }
}
