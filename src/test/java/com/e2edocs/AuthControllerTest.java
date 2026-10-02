package com.e2edocs;

import com.e2edocs.dto.AuthResponse;
import com.e2edocs.dto.LoginRequest;
import com.e2edocs.dto.UserResponse;
import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login returns token and user payload")
    void testLoginSuccess() throws Exception {
        UserResponse userResponse = new UserResponse(
                "u-001",
                "Sarah Chen",
                "sarah.chen@example.com",
                UserRole.SUPER_ADMIN,
                "Engineering & Product",
                "dept-006",
                UserStatus.ACTIVE,
                null,
                Instant.now(),
                Instant.now()
        );
        AuthResponse authResponse = new AuthResponse(userResponse, "test-jwt-token-string");

        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(authResponse);

        LoginRequest request = new LoginRequest("sarah.chen@example.com", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token-string"))
                .andExpect(jsonPath("$.user.email").value("sarah.chen@example.com"))
                .andExpect(jsonPath("$.user.role").value("super_admin"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns token and user payload")
    void testRegisterSuccess() throws Exception {
        UserResponse userResponse = new UserResponse(
                "u-002",
                "Jane Doe",
                "jane.doe@example.com",
                UserRole.ADMIN,
                null,
                null,
                UserStatus.ACTIVE,
                null,
                Instant.now(),
                Instant.now()
        );
        AuthResponse authResponse = new AuthResponse(userResponse, "test-register-jwt-token");

        when(authService.register(any(com.e2edocs.dto.RegisterRequest.class), anyString())).thenReturn(authResponse);

        com.e2edocs.dto.RegisterRequest request = new com.e2edocs.dto.RegisterRequest(
                "Jane Doe", "jane.doe@example.com", "Password123!", "Acme Corp"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-register-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.user.role").value("admin"));
    }

    @Test
    @DisplayName("GET /api/auth/signup/status returns availability flag")
    void testGetSignupStatus() throws Exception {
        when(authService.getSignupStatus()).thenReturn(new com.e2edocs.dto.SignupStatusResponse(true));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/signup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
