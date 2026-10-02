package com.e2edocs;

import com.e2edocs.dto.CreateUserRequest;
import com.e2edocs.dto.RegisterRequest;
import com.e2edocs.entity.AuditLog;
import com.e2edocs.entity.Organization;
import com.e2edocs.entity.User;
import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.repository.AuditLogRepository;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.security.JwtService;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Fresh database: GET /api/auth/signup/status returns available=true")
    void testSignupStatusFreshDatabase() throws Exception {
        mockMvc.perform(get("/api/auth/signup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("2. First registration succeeds, creates Organization, and assigns ADMIN role")
    void testSuccessfulFirstRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Johnson",
                "alice.johnson@acme-corp.com",
                "StrongPassword123!",
                "Acme Corporation"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.id").isNotEmpty())
                .andExpect(jsonPath("$.user.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.user.email").value("alice.johnson@acme-corp.com"))
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andExpect(jsonPath("$.user.status").value("active"));

        // Verify DB persistence
        assertEquals(1, userRepository.count());
        assertEquals(1, organizationRepository.count());

        User savedUser = userRepository.findByEmailIgnoreCase("alice.johnson@acme-corp.com").orElseThrow();
        assertEquals("Alice Johnson", savedUser.getName());
        assertEquals(UserRole.ADMIN, savedUser.getRole(), "First user MUST be assigned ADMIN role");
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());

        // Password must be BCrypt hashed, never plaintext
        assertNotEquals("StrongPassword123!", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("StrongPassword123!", savedUser.getPasswordHash()));

        // Organization must be created and linked
        Organization savedOrg = organizationRepository.findById(savedUser.getOrganizationId()).orElseThrow();
        assertEquals("Acme Corporation", savedOrg.getName());
        assertTrue(savedOrg.getCode().startsWith("acme-corporation"));
        assertEquals("acme-corp.com", savedOrg.getDomain());

        // Audit log must exist
        List<AuditLog> auditLogs = auditLogRepository.findByOrganizationId(savedOrg.getId());
        assertFalse(auditLogs.isEmpty(), "Audit log should record bootstrap registration");
    }

    @Test
    @DisplayName("3. After initialization: GET /api/auth/signup/status returns available=false")
    void testSignupStatusAfterInitialization() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Johnson", "alice@acme.com", "StrongPassword123!", "Acme Corp"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/signup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("4. Second and third public registrations are rejected once initialized")
    void testSecondAndThirdRegistrationRejected() throws Exception {
        // First registration (Main Admin)
        RegisterRequest first = new RegisterRequest(
                "Main Admin", "admin@primary.com", "Password123!", "Primary Org"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        // Second registration attempt
        RegisterRequest second = new RegisterRequest(
                "Second User", "second@other.com", "Password123!", "Other Org"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Public registration is disabled. The system administrator setup is already complete."));

        // Third registration attempt
        RegisterRequest third = new RegisterRequest(
                "Third User", "third@another.com", "Password123!", "Another Org"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(third)))
                .andExpect(status().isBadRequest());

        // Invariant: Exactly 1 user and 1 organization exist in DB
        assertEquals(1, userRepository.count());
        assertEquals(1, organizationRepository.count());
    }

    @Test
    @DisplayName("5. Main Admin can invite/create additional users within their organization")
    void testAdminCanInviteUsersWithinSameOrganization() throws Exception {
        // Bootstrap Main Admin
        RegisterRequest adminReq = new RegisterRequest(
                "Main Admin", "admin@company.com", "Password123!", "Company Inc"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isOk());

        User adminUser = userRepository.findByEmailIgnoreCase("admin@company.com").orElseThrow();
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
        String adminToken = jwtService.generateToken(adminPrincipal);

        // Main Admin invites an OPERATOR / USER
        CreateUserRequest inviteReq = new CreateUserRequest(
                "Bob Operator", "bob.operator@company.com", UserRole.USER, null, "TempPass123!"
        );

        mockMvc.perform(post("/api/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob.operator@company.com"))
                .andExpect(jsonPath("$.role").value("user"));

        // Verify invited user is associated with Main Admin's organization
        User invitedUser = userRepository.findByEmailIgnoreCase("bob.operator@company.com").orElseThrow();
        assertEquals(adminUser.getOrganizationId(), invitedUser.getOrganizationId());
        assertEquals(2, userRepository.count());
        assertEquals(1, organizationRepository.count(), "Inviting users does not create additional organizations");
    }

    @Test
    @DisplayName("6. Missing required fields validation")
    void testMissingFieldsValidation() throws Exception {
        RegisterRequest missingName = new RegisterRequest("", "valid@email.com", "Password123!", "Org Name");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingName)))
                .andExpect(status().isBadRequest());

        RegisterRequest missingEmail = new RegisterRequest("Name", "", "Password123!", "Org Name");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingEmail)))
                .andExpect(status().isBadRequest());

        RegisterRequest missingOrg = new RegisterRequest("Name", "valid@email.com", "Password123!", "");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingOrg)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7. Invalid email format validation")
    void testInvalidEmailFormat() throws Exception {
        RegisterRequest invalidEmail = new RegisterRequest("Name", "not-an-email", "Password123!", "Org Name");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8. Weak/short password validation (< 8 characters)")
    void testWeakPassword() throws Exception {
        RegisterRequest weakPass = new RegisterRequest("Name", "valid@email.com", "short", "Org Name");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPass)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("9. Concurrent first-registration race protection: exactly ONE Main Admin created")
    void testConcurrentFirstRegistrationRaceProtection() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    RegisterRequest req = new RegisterRequest(
                            "User " + index,
                            "user" + index + "@race-condition.com",
                            "Password123!",
                            "Org " + index
                    );
                    authService.register(req, "127.0.0.1");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one registration must succeed concurrently");
        assertEquals(threadCount - 1, failureCount.get(), "All other concurrent registrations must fail");
        assertEquals(1, userRepository.count(), "DB must contain exactly one user");
        assertEquals(1, organizationRepository.count(), "DB must contain exactly one organization");
    }
}
