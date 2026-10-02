package com.e2edocs;

import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.security.JwtService;
import com.e2edocs.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTest {

    private final JwtService jwtService = new JwtService(
            "e2edocs-super-secret-jwt-signing-key-minimum-256-bits-length-token-key-2026",
            3600000
    );

    @Test
    @DisplayName("Generate and validate JWT token with correct user claims")
    void testJwtTokenGenerationAndValidation() {
        UserPrincipal principal = new UserPrincipal(
                "u-001",
                "org-001",
                "Sarah Chen",
                "sarah.chen@example.com",
                "hashedpwd",
                UserRole.SUPER_ADMIN,
                UserStatus.ACTIVE
        );

        String token = jwtService.generateToken(principal);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));

        assertEquals("u-001", jwtService.getUserIdFromToken(token));
        assertEquals("sarah.chen@example.com", jwtService.getEmailFromToken(token));
    }
}
