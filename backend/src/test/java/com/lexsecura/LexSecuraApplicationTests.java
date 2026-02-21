package com.lexsecura;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Disabled("Requires Docker with Testcontainers (Postgres + MinIO)")
class LexSecuraApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16"))
            .withDatabaseName("lexsecura_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> minio = new GenericContainer<>(
            DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.s3.endpoint", () ->
                "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("app.s3.access-key", () -> "minioadmin");
        registry.add("app.s3.secret-key", () -> "minioadmin");
        registry.add("app.s3.bucket", () -> "evidences-test");
        registry.add("app.s3.region", () -> "us-east-1");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:18080/realms/test");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost:18080/realms/test/protocol/openid-connect/certs");
    }

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors
    }
}
