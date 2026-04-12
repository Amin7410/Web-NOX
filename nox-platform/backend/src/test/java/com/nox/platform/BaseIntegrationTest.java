package com.nox.platform;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("nox_test")
            .withUsername("nox")
            .withPassword("nox_secret")
            .withReuse(true);

    @Container
    @ServiceConnection
    protected static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    static {
        postgres.start();
        redis.start();
    }
}
