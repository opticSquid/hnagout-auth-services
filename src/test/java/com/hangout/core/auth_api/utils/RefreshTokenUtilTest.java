package com.hangout.core.auth_api.utils;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.hangout.core.auth_api.helpers.RefreshTokenUtilTestImpl;

import io.jsonwebtoken.ExpiredJwtException;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class RefreshTokenUtilTest {
    @Autowired
    private RefreshTokenUtil refreshTokenUtil;
    @Autowired
    private RefreshTokenUtilTestImpl refreshTokenUtilTestImpl;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void testValidateToken_passes_with_long_term_valid_token() {
        refreshTokenUtilTestImpl.setExpiries(5000, 1000);
        String inputRefToken = refreshTokenUtilTestImpl.generateToken("test", UUID.randomUUID());
        assertTrue(refreshTokenUtil.validateToken(inputRefToken));
    }

    @Test
    void testValidateToken_throws_exception_with_expired_long_term_token() throws InterruptedException {
        refreshTokenUtilTestImpl.setExpiries(500, 100);
        String inputRefToken = refreshTokenUtilTestImpl.generateToken("test", UUID.randomUUID());
        Thread.sleep(2000);
        assertThrows(ExpiredJwtException.class, () -> refreshTokenUtil.validateToken(inputRefToken));
    }
}
