package com.lexsecura.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.IntegrationTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Disabled("Requires Docker with Testcontainers (Postgres + MinIO)")
class IamControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TEST_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void getMe_withValidJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .with(jwt().jwt(j -> j
                                .subject(TEST_USER_ID.toString())
                                .claim("org_id", TEST_ORG_ID.toString())
                                .claim("roles", List.of("ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()));
    }

    @Test
    void getMe_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrg_withAdminRole_shouldReturn201() throws Exception {
        String body = """
            {"name": "Test Org", "slug": "test-org"}
            """;

        mockMvc.perform(post("/api/v1/orgs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().jwt(j -> j
                                .subject(TEST_USER_ID.toString())
                                .claim("org_id", TEST_ORG_ID.toString())
                                .claim("roles", List.of("ADMIN")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Org"))
                .andExpect(jsonPath("$.slug").value("test-org"));
    }

    @Test
    void createOrg_withContributorRole_shouldReturn403() throws Exception {
        String body = """
            {"name": "Test Org", "slug": "test-org-2"}
            """;

        mockMvc.perform(post("/api/v1/orgs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(jwt().jwt(j -> j
                                .subject(TEST_USER_ID.toString())
                                .claim("org_id", TEST_ORG_ID.toString())
                                .claim("roles", List.of("CONTRIBUTOR")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listProducts_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listProducts_withValidJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(jwt().jwt(j -> j
                                .subject(TEST_USER_ID.toString())
                                .claim("org_id", TEST_ORG_ID.toString())
                                .claim("roles", List.of("ADMIN")))))
                .andExpect(status().isOk());
    }
}
